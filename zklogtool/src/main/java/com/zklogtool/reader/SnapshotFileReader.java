/* 
 * Copyright 2014 Alen Caljkusic.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zklogtool.reader;

import com.zklogtool.data.DataState;
import com.zklogtool.data.Transaction;
import com.zklogtool.data.TransactionIterator;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import static java.nio.ByteBuffer.wrap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import static org.apache.jute.BinaryInputArchive.getArchive;
import org.apache.jute.InputArchive;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import org.apache.zookeeper.server.DataNode;
import org.apache.zookeeper.server.DataTree;
import org.apache.zookeeper.server.persistence.FileHeader;

/**
 * <code>SnapshotFileReader</code> reads Zookeeper snapshot files.
 * <br>
 * Snapshot is Zookeeper data tree dump. Because servers keep executing requests
 * while taking a snapshot, the data tree changes as the snapshot is taken. Such
 * snapshots are fuzzy because they do not necessarily reflect the exact state
 * of the data tree at any particular point in time.
 * <br>
 * Zookeeper data tree is represented by <code>DataState</code> class in
 * zklogtool framework. <code>SnapshotFileReader</code> can return fuzzy
 * <code>DataState</code> based only on snapshot or it can take the snapshot any
 * apply transactions to the point when it is guaranteed that
 * <code>DataState</code> represents data tree at one particular point it time.
 *
 */
public class SnapshotFileReader {

    static private Logger logger = getLogger(SnapshotFileReader.class);

    public final static int SNAP_MAGIC = wrap("ZKSN".getBytes()).getInt();

    final File snapshotFile;
    final long TS;

    /**
     *
     * @param snapshotFile Zookeeper snapshot file.
     * @param TS Zxid of last transaction completely committed before snapshot
     * took place. It is written in snapshot filename.
     */
    public SnapshotFileReader(File snapshotFile, long TS) {
        this.snapshotFile = snapshotFile;
        this.TS = TS;
    }

    public File getSnapshotFile() {
        return snapshotFile;
    }

    /**
     * Reads snapshot file and returns <code>DataState</code> based on it.
     *
     * @return Returns <code>DataState</code> based only on snapshot file.
     * @throws CRCValidationException Thrown if CRC validation failed. Snapshot
     * is probably corrupted.
     * @throws IOException Thrown if there is a problem with reading snapshot
     * file.
     */
    public DataState readFuzzySnapshot() throws CRCValidationException, IOException {

        DataTree dt = new DataTree();
        Map<Long, Integer> sessions = new HashMap<Long, Integer>();

        InputStream snapIS = null;
        CheckedInputStream crcIn = null;

        try {
            logger.info("Reading snapshot " + snapshotFile);
            snapIS = new BufferedInputStream(new FileInputStream(snapshotFile));
            crcIn = new CheckedInputStream(snapIS, new Adler32());
            InputArchive ia = getArchive(crcIn);

            FileHeader header = new FileHeader();
            header.deserialize(ia, "fileheader");
            if (header.getMagic() != SNAP_MAGIC) {
                throw new IOException("Mismatching magic headers "
                        + header.getMagic()
                        + " !=  " + SNAP_MAGIC);
            }

            int count = ia.readInt("count");

            while (count > 0) {
                long id = ia.readLong("id");
                int to = ia.readInt("timeout");
                sessions.put(id, to);
                count--;
            }

            dt.deserialize(ia, "tree");

            long checkSum = crcIn.getChecksum().getValue();
            long val = ia.readLong("val");
            if (val != checkSum) {
                throw new CRCValidationException("CRC corruption in snapshot");
            }

        } catch (IOException e) {
            logger.warn("Problem reading snap file " + snapshotFile, e);

            throw new IOException(e);

        } finally {
            if (snapIS != null) {
                snapIS.close();
            }
            if (crcIn != null) {
                crcIn.close();
            }
        }

        long lastZxid = -1;

        DataState dataState = new DataState(dt, sessions, lastZxid);

        Iterator<Entry<String, DataNode>> it = dataState.getNodes().entrySet().iterator();
        while (it.hasNext()) {

            Entry<String, DataNode> e = it.next();

            long tempZxid = e.getValue().stat.getCzxid();

            if (tempZxid > lastZxid) {
                lastZxid = tempZxid;
            }

            tempZxid = e.getValue().stat.getPzxid();
            if (tempZxid > lastZxid) {
                lastZxid = tempZxid;
            }

            tempZxid = e.getValue().stat.getMzxid();
            if (tempZxid > lastZxid) {
                lastZxid = tempZxid;
            }

        }

        dataState.setLastZxid(lastZxid);

        return dataState;

    }

    /**
     * Reads snapshot file and applies transactions to ensure that returned
     * <code>DataState</code> is not fuzzy.
     *
     * @param transactionIterator Transaction iterator of transactions that are
     * to be applied if needed to ensure returned <code>DataState</code> is not
     * fuzzy.
     * @return <code>DataState</code> that is not fuzzy.
     * @throws CRCValidationException Thrown if CRC validation failed, snapshot
     * is probably corrupted.
     * @throws Exception Thrown if there is a problem while applying
     * transactions.
     * @throws IOException Thrown if there is a problem with reading snapshot
     * file.
     */
    public DataState restoreDataState(TransactionIterator transactionIterator) throws CRCValidationException, IOException, Exception {

        DataState fuzzy = readFuzzySnapshot();
        long lastZxid = fuzzy.getLastZxid();
        long currentZxid = TS + 1;
        Transaction t;

        // rewind iterator to right zxid
        do {

            switch (transactionIterator.nextTransactionState()) {

                case OK:
                    t = transactionIterator.next();
                    break;
                case EMPTY:
                    throw new Exception("No tranasction");
                default:
                    throw new Exception("Problem while reading transactions");

            }

        } while (t.getTxnHeader().getZxid() <= TS);

        //apply all transactions in between 
        //there should be a check if there are any transactions in between, if not sessions may not be correct
        logger.debug("Current zxid: " + currentZxid);
        logger.debug("Last zxid: " + lastZxid);
        logger.debug("Current txn zxid: " + t.getTxnHeader().getZxid());

        while (currentZxid <= lastZxid) {

            //check ordering
            if (t.getTxnHeader().getZxid() != currentZxid) {
                throw new Exception("Transactions not in order");
            }

            //apply transaction
            fuzzy.processTransaction(t);

            //read next
            switch (transactionIterator.nextTransactionState()) {

                case OK:
                    t = transactionIterator.next();
                    break;
                case EMPTY:
                    throw new Exception("No tranasction");
                default:
                    throw new Exception("Problem while reading transactions");

            }

            //set next expected transaction
            currentZxid++;

        }

        return fuzzy;

    }

    /*
    
     Uses reflection to construct DataState because it has only private constructor
     DataState has only private constructor because API users should never try to
     initialize their own instance
    
     Only way to get DataState should be using restoreDataState and readFuzzySnapshot 
     methods of this class
    
     */
    private DataState getDataState() {

        try {

            //create data tree
            DataTree dataTree = new DataTree();

            //inject logger
            Field field = dataTree.getClass().getDeclaredField("LOG");
            field.setAccessible(true);
            //field.set(TS, field);

            //create data state
            Constructor ctor = DataState.class.getDeclaredConstructors()[0];

            ctor.setAccessible(true);
            DataState dataState = (DataState) ctor.newInstance(null, null, null);

            return dataState;

        } catch (Exception x) {
            //
        }

        return null;

    }

}
