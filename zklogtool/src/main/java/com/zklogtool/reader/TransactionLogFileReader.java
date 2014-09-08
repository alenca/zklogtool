/* 
 * Copyright 2014 Alen Čaljkušić.
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

import com.zklogtool.data.Transaction;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import static java.nio.ByteBuffer.wrap;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.apache.jute.BinaryInputArchive;
import org.apache.jute.InputArchive;
import org.apache.jute.Record;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import org.apache.zookeeper.server.persistence.FileHeader;
import org.apache.zookeeper.server.persistence.FileTxnLog;
import static org.apache.zookeeper.server.util.SerializeUtils.deserializeTxn;
import org.apache.zookeeper.txn.TxnHeader;

/**
 * <code>TransactionLogFileReader</code> is used to read transactions from
 * transaction log files. Provided interface is similar to iterator interface
 * and it has reset method. Instead of standard <code>hasNext()</code> method,
 * <code>getNextTransaction()</code> is used to provide info about next element
 * in iteration. <code>getNextTransaction()</code> method throws descriptive
 * exception if next transaction is not ready. Transactions in transaction log
 * files are written one after another as byte sequences. File pointer always
 * points in front of transaction. File pointer is not part of an API but it is
 * used to describe behavior of methods in this class.
 * <br>
 * Zookeeper servers use transaction log to persist transactions. Before
 * accepting a proposal, a server persists transaction in the proposal to the
 * transaction log, a file on the local disk of the server to which transactions
 * are appended in order.
 *
 */
public class TransactionLogFileReader {

    private static Logger logger = getLogger(TransactionLogFileReader.class);
    private final static int TXNLOG_MAGIC = wrap("ZKLG".getBytes()).getInt();
    private final File transactionLogFile;
    private InputArchive ia;
    private RandomAccessFile raf;
    private FileHeader header;

    private long resetFilePointer, lastTransactionFilePointer;

    /**
     *
     * @param transactionLogFile Transaction log file.
     * @throws FileNotFoundException Thrown if file is not found.
     * @throws IOException Thrown if there is a problem with reading
     * <code>transactionLogFile</code>.
     */
    public TransactionLogFileReader(File transactionLogFile) throws FileNotFoundException, IOException {

        this.transactionLogFile = transactionLogFile;

        raf = new RandomAccessFile(transactionLogFile, "r");
        ia = new BinaryInputArchive(raf);

        header = new FileHeader();
        header.deserialize(ia, "fileheader");

        if (header.getMagic() != TXNLOG_MAGIC) {

            throw new IOException("Mismatching magic headers "
                    + header.getMagic()
                    + " != " + FileTxnLog.TXNLOG_MAGIC);
        }

        resetFilePointer = raf.getFilePointer();
        lastTransactionFilePointer = resetFilePointer;

    }

    /**
     * Returns next transaction. If any exception is thrown file pointer remains
     * on the place as it was before method call. Consecutive call may succeed
     * without exception if file is updated in meantime and reason of previous
     * exception is removed. It is guaranteed that no transactions are skipped.
     *
     * @return Transaction written after file pointer.
     * @throws IncompleteTransactionException Thrown if next transaction is not
     * fully written to transaction log.
     * @throws CRCValidationException Thrown if CRC validation failed.
     * @throws IOException Thrown if there is an IO problem.
     */
    public Transaction getNextTransaction() throws IncompleteTransactionException, CRCValidationException, IOException {

        long crcValue;
        byte[] bytes;
        int len;
        byte EOF;

        try {

            crcValue = ia.readLong("crcvalue");
            len = ia.readInt("txtEntry");

        } catch (IOException e) {

            //end of file, otherwise both values should be present in previously padded space
            raf.seek(lastTransactionFilePointer);
            return null;
        }

        if (crcValue == 0 && len == 0) {
            //we are in padded space (or brutal corruption)
            raf.seek(lastTransactionFilePointer);
            return null;
        }

        try {

            bytes = new byte[len];
            raf.readFully(bytes);
            EOF = ia.readByte("EOF");

        } catch (IOException e) {

            //not whole transaction has been written jet
            raf.seek(lastTransactionFilePointer);
            throw new IncompleteTransactionException("Problem with reading file before transaction end", e);
        }

        if (EOF == 0) {
            raf.seek(lastTransactionFilePointer);
            throw new IncompleteTransactionException("Transaction delimiter byte not set");
        }

        if (EOF != 'B') {
            raf.seek(lastTransactionFilePointer);
            throw new CRCValidationException("Transaction delimiter byte wrong");
        }

        Checksum crc = new Adler32();
        crc.update(bytes, 0, bytes.length);
        if (crcValue != crc.getValue()) {
            raf.seek(lastTransactionFilePointer);
            throw new CRCValidationException("Transaction CRC validation failed");
        }

        TxnHeader hdr = new TxnHeader();
        Record record = deserializeTxn(bytes, hdr);

        lastTransactionFilePointer = raf.getFilePointer();
        return new Transaction(hdr, record);

    }

    /**
     * Resets file pointer to the beginning of the transaction log file.
     *
     * @throws IOException Thrown if there is an IO problem.
     */
    public void reset() throws IOException {

        raf.seek(resetFilePointer);

    }

    public FileHeader getFileHeader() {
        return header;
    }

    public File getTransactionLogFile() {
        return transactionLogFile;
    }

}
