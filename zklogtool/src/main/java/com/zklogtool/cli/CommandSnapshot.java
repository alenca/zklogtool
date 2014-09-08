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
package com.zklogtool.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.zklogtool.data.DataDirTransactionLogFileList;
import com.zklogtool.data.DataState;
import com.zklogtool.data.Transaction;
import com.zklogtool.data.TransactionIterator;
import com.zklogtool.data.TransactionLog;
import com.zklogtool.data.TransactionLogFileList;
import com.zklogtool.data.TransactionState;
import com.zklogtool.printer.DataDecoder;
import com.zklogtool.printer.DataNodePrinter;
import com.zklogtool.printer.UnicodeDecoder;
import com.zklogtool.reader.SnapshotFileReader;
import com.zklogtool.reader.TransactionLogReaderFactory;
import com.zklogtool.util.DataDirHelper;
import com.zklogtool.util.PropertiesReader;
import static com.zklogtool.util.Util.getZxidFromName;
import java.io.File;
import java.io.IOException;
import static java.lang.Long.parseLong;
import static java.lang.System.exit;
import static java.text.Collator.getInstance;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.apache.zookeeper.server.DataNode;

/**
 * Represents <b>snapshot</b> command and holds logic for <b>snapshot</b>
 * command execution. It is also used by jCommander for command parameters.
 * <br>
 * Snapshot command can printout contents of single Zookeeper snapshot file.
 * Snapshot file is taken lazily so it may not represent data tree at any point
 * of time. Snapshot command can reconstruct Zookeeper data tree after a certain
 * zxid by taking one of the snapshots and applying transactions from
 * transaction log files to it. Reconstructed data tree is not fuzzy and it
 * corresponds to Zookeeper data tree at some point in time. To reconstruct data
 * tree one must provide <code>Arguments.DATA_DIR</code> argument.
 *
 */
@Parameters(commandDescription = "Display zookeeper data tree at certain time")
public class CommandSnapshot {

    /**
     * Zxid up to which data tree should be restored. Strings first and last can
     * also be used. In that case, first or last data tree state that it can be
     * reconstructed from snapshots and transaction logs will get displayed.
     * Default value is last.
     *
     */
    @Parameter(names = Arguments.ZXID, description = "Hex value of last commited zxid. Strings first and last can also be used")
    public String zxid = "last";

    /**
     * Directory where snapshot and transaction log files are stored. If
     * snapshot and transaction log files are not stored in same directory use
     * this option to point to directory where snapshot files are stored.
     *
     */
    @Parameter(names = Arguments.DATA_DIR, description = "Zookeeper data direcory path")
    public String dataDir;

    /**
     * Directory where transaction log files are stored. Zookeeper can be
     * configured to store transaction log files and snapshots in different
     * directories. In that case use this option to point to directory where
     * transaction log files are stored.
     *
     */
    @Parameter(names = Arguments.DATA_LOG_DIR, description = "Zookeeper log direcory path. If not provided dataDir is used")
    public String dataLogDir;

    /**
     * Zookeeper snapshot file. Snapshot file is taken lazily so it may not
     * represent data tree at any point of time. Therefor it is called fuzzy
     * snapshot.
     *
     */
    @Parameter(names = Arguments.SNAP_FILE, description = "Zookeeper snapshot file. Data is fuzzy.")
    public String snapFile;

    /**
     * Zookeeper configuration file. Zookeeper configuration file tells
     * zookeeper where to store transaction log files and snapshot files.
     * zklogtool can read locations from configuration file.
     *
     */
    @Parameter(names = Arguments.PROPERTIES_FILE, description = "Zookeeper configuration file path")
    public String propertiesFile;

    /**
     * Decoder that converts znodes byte array to <code>String</code> that can
     * be printed to output. Znode holds data in form of a byte array. That byte
     * array must be decoded in something that can be printed to output in order
     * to display it.
     *
     */
    @Parameter(names = Arguments.DATA_DECODER, description = "Decoder used to display znode's data byte array")
    public String dataDecoder = "UnicodeDecoder";

    /**
     * Holds logic for <b>snapshot</b> command execution.
     *
     */
    public void execute() {

        final StringBuilder print = new StringBuilder();
        DataDecoder decoder = null;
        DataState dataState = null;

        if (dataDecoder.contentEquals("UnicodeDecoder")) {
            decoder = new UnicodeDecoder();
        } else {

            System.err.println("Decoder not recognized");
            exit(1);
        }

        final DataNodePrinter printer = new DataNodePrinter(print, decoder);

        if (snapFile != null) {

            File snapshotFile = new File(snapFile);
            if (!checkFileValid(snapshotFile)) {
                exit(1);
            }

            SnapshotFileReader reader = new SnapshotFileReader(snapshotFile, 0);
            try {
                dataState = reader.readFuzzySnapshot();
            } catch (IOException ex) {
                System.err.println("Problem while reading file or corruption: " + snapshotFile.getAbsolutePath());
                exit(1);
            }

        } else {

            TransactionLog transactionLog = null;
            TransactionLogReaderFactory factory = new TransactionLogReaderFactory();
            File transactionLogDir = null;
            File snapshotDir = null;
            long zxidLong = 0;

            if (zxid != null && !(zxid.contentEquals("last") || zxid.contentEquals("first"))) {
                
                if(zxid.startsWith("0x")){
                    zxidLong = parseLong(zxid.substring(2),16);
                }else{
                    zxidLong = parseLong(zxid);
                }
                
            }

            if (dataDir != null) {
                transactionLogDir = new File(dataDir);
                snapshotDir = new File(dataDir);
            }
            if (dataLogDir != null) {
                transactionLogDir = new File(dataLogDir);
            }
            if (propertiesFile != null) {

                File properties = new File(propertiesFile);
                if (!checkFileValid(properties)) {
                    exit(1);
                }

                PropertiesReader propertiesReader = null;

                try {
                    propertiesReader = new PropertiesReader(properties);
                } catch (IOException e) {
                    System.err.println("Problem with reading properties file: " + properties.getAbsolutePath());
                    exit(1);
                }

                String transactionLogDirPath = propertiesReader.getTransactionLogDir();
                String snapshotDirPath = propertiesReader.getSnapshotDir();

                if (transactionLogDirPath == null || snapshotDirPath == null) {
                    System.err.println("Problem in properties file: " + properties.getAbsolutePath());
                    exit(1);
                }

                transactionLogDir = new File(transactionLogDirPath);
                snapshotDir = new File(snapshotDirPath);

            }

            if (!checkDirectoryValid(transactionLogDir)) {
                exit(1);
            }

            if (!checkDirectoryValid(snapshotDir)) {
                exit(1);
            }

            DataDirHelper dataDirHelper = new DataDirHelper(transactionLogDir, snapshotDir);
            List<File> snapshots = dataDirHelper.getSortedSnapshotList();
            TransactionLogFileList l = new DataDirTransactionLogFileList(transactionLogDir);
            transactionLog = new TransactionLog(l, factory);

            if (snapshots.isEmpty()) {
                System.err.println("No snapshot files found");
                exit(1);
            }

            //determine what snapshot file to read
            File snapFile = null;

            if (zxid.contentEquals("first")) {
                snapFile = snapshots.get(0);
            } else if (zxid.contentEquals("last")) {
                if (snapshots.size() >= 2) {
                    snapFile = snapshots.get(snapshots.size() - 2);
                } else {
                    snapFile = snapshots.get(0);
                }
            } else {

                int i = snapshots.size() - 1;
                while (i >= 0) {

                    long snapZxid = getZxidFromName(snapshots.get(i).getName());

                    if (snapZxid <= zxidLong) {

                        if (i == 0) {
                            snapFile = snapshots.get(0);
                        } else {
                            snapFile = snapshots.get(i - 1);
                        }

                        break;

                    }

                    i--;

                }

            }

            if(snapFile==null){
                System.err.println("Not enough data to reconstruct data tree.");
                exit(1);
            }
            long TS = getZxidFromName(snapFile.getName());

            
            SnapshotFileReader snapReader = new SnapshotFileReader(snapFile, TS);

            try {
                dataState = snapReader.restoreDataState(transactionLog.iterator());
            } catch (Exception ex) {
                System.err.println("Problem while reading transaction log: " + ex.getMessage());
                exit(1);
            }

            //set iterator to last zxid
            TransactionIterator iterator = transactionLog.iterator();
            Transaction t;

            do {

                t = iterator.next();
                //treba i provjera next transaction state
            } while (t.getTxnHeader().getZxid() < TS);

            
            
            
            //rewind
            if (zxid.contentEquals("last")) {
                while (iterator.nextTransactionState() == TransactionState.OK) {
                    dataState.processTransaction(iterator.next());
                }

            } else if (!zxid.contentEquals("first")) {

                while (iterator.nextTransactionState() == TransactionState.OK && dataState.getLastZxid() < zxidLong) {
                    dataState.processTransaction(iterator.next());
                }
            }

            //check if null
        }

        //print dataState lexicograph ordering
        Map<String, DataNode> nodes = dataState.getNodes();

        //create sorted collection
        Collection<String> paths = new TreeSet<String>(getInstance());
        Iterator<Map.Entry<String, DataNode>> it = dataState.getNodes().entrySet().iterator();
        while (it.hasNext()) {

            Map.Entry<String, DataNode> entry = it.next();
            paths.add(entry.getKey());

        }

        System.out.println("Last processed zxid: 0x" + Long.toString(dataState.getLastZxid(),16)+System.lineSeparator());

        //print sessions
        System.out.println("Sessions:"+System.lineSeparator());
        Iterator<Map.Entry<Long, Integer>> se = dataState.getSessions().entrySet().iterator();
        while (se.hasNext()) {

            Map.Entry<Long, Integer> entry = se.next();
            System.out.println("id 0x"+Long.toString(entry.getKey(),16)+" timeout "+entry.getValue());

        }

        System.out.println(System.lineSeparator());
        
        //print data nodes
        System.out.println("Data :"+System.lineSeparator());
        for (String path : paths) {

            System.out.println("Path:\t\t" + path);
            printer.printDataNode(nodes.get(path), dataState);
            System.out.println(print);
            print.setLength(0);

        }

    }

    private boolean checkFileValid(File file) {

        if (file.isDirectory()) {

            System.err.println(file + " is directory");
            return false;

        } else if (!file.isFile()) {

            System.err.println("File " + file + " not found");
            return false;

        } else if (!file.canRead()) {

            System.err.println("File " + file + " not readable");
            return false;

        }

        return true;

    }

    private boolean checkDirectoryValid(File directory) {

        if (directory.isFile()) {

            System.err.println(directory + " is file");
            return false;

        } else if (!directory.isDirectory()) {

            System.err.println("Directory " + directory + " not found");
            return false;

        } else if (!directory.canRead()) {

            System.err.println("Directory " + directory + " not readable");
            return false;

        }

        return true;

    }

}
