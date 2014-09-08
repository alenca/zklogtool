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
package com.zklogtool.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.zklogtool.data.DataDirTransactionLogFileList;
import com.zklogtool.data.Transaction;
import com.zklogtool.data.TransactionIterator;
import com.zklogtool.data.TransactionLog;
import com.zklogtool.data.TransactionLogFileList;
import com.zklogtool.data.TransactionState;
import com.zklogtool.monitor.TransactionListener;
import com.zklogtool.monitor.TransactionMonitor;
import com.zklogtool.printer.DataDecoder;
import com.zklogtool.printer.TransactionPrinter;
import com.zklogtool.printer.UnicodeDecoder;
import com.zklogtool.reader.TransactionLogReaderFactory;
import com.zklogtool.util.PropertiesReader;
import java.io.File;
import java.io.IOException;
import static java.lang.System.exit;

/**
 * Represents <b>log</b> command and holds logic for <b>log</b> command
 * execution. It is also used by jCommander for command parameters.
 *
 */
@Parameters(commandDescription = "Display transaction log entries")
public class CommandLog {

    /**
     * If true zklogtool will monitor transaction log directory and printout new
     * transactions as they are written to disk. Tool will never exit on it's
     * own.
     *
     */
    @Parameter(names = Arguments.FOLLOW, description = "Output appended data as the transactions are written to logs")
    public boolean follow = false;

    /**
     * When <code>Arguments.FOLLOW</code> option is used user may choose to
     * printout only transactions that were written to disk after zklogtool
     * started.
     *
     */
    @Parameter(names = Arguments.START_WITH_LAST_TRANSACTION, description = "Start printout from last written transaction. Can only be used with follow option")
    public boolean startWithLastTransaction = false;

    /**
     * Directory that holds Zookeeper transaction log files.
     *
     */
    @Parameter(names = Arguments.DATA_LOG_DIR, description = "Zookeeper log direcory path")
    public String dataLogDir;

    /**
     * Single Zookeeper transaction log file.
     *
     */
    @Parameter(names = Arguments.LOG_FILE, description = "Zookeeper transaction log file path")
    public String logFile;

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
     * Holds logic for <b>log</b> command execution.
     *
     * @throws InterruptedException Thrown if interrupted during execution.
     */
    public void execute() throws InterruptedException {

        TransactionLog transactionLog = null;
        TransactionLogReaderFactory factory = new TransactionLogReaderFactory();
        final StringBuilder print = new StringBuilder();
        DataDecoder decoder = null;

        if (dataDecoder.contentEquals("UnicodeDecoder")) {
            decoder = new UnicodeDecoder();
        } else {

            System.err.println("Decoder not recognized");
            exit(1);
        }

        final TransactionPrinter printer = new TransactionPrinter(print, decoder);

        if (dataLogDir != null) {

            File transactionLogDir = new File(dataLogDir);

            if (!checkDirectoryValid(transactionLogDir)) {
                exit(1);
            }

            TransactionLogFileList l = new DataDirTransactionLogFileList(transactionLogDir);

            transactionLog = new TransactionLog(l, factory);

        } else if (logFile != null) {

            File transactionLogFile = new File(logFile);

            //check file
            if (!checkFileValid(transactionLogFile)) {
                exit(1);
            }

            transactionLog = new TransactionLog(transactionLogFile, factory);

        } else if (propertiesFile != null) {

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

            if (transactionLogDirPath == null) {

                System.err.println("Problem in properties file: " + properties.getAbsolutePath());
                exit(1);

            }

            File transactionLogDir = new File(transactionLogDirPath);

            if (!checkDirectoryValid(transactionLogDir)) {
                exit(1);
            }

            TransactionLogFileList l = new DataDirTransactionLogFileList(transactionLogDir);

            transactionLog = new TransactionLog(l, factory);

        }

        if (follow) {

            TransactionMonitor ts = new TransactionMonitor(transactionLog);

            ts.addListener(new TransactionListener() {

                @Override
                public void onTransaction(Transaction t) {

                    printer.print(t);
                    System.out.println(print);
                    print.setLength(0);

                }

                @Override
                public void onPartialTransaction() {

                    //do nothing
                }

                @Override
                public void onCorruption() {

                    //print error and exit
                    System.err.println("Data corruption");
                    exit(1);

                }

            });

            if (startWithLastTransaction) {

                ts.startAtLastTransaction();

            } else {

                ts.startAtFirstTransaction();

            }

            //there must be a better way
            synchronized (this) {
                wait();
            }

            //not really needed
            exit(0);

        } else {

            TransactionIterator transactionIterator = transactionLog.iterator();

            while (transactionIterator.nextTransactionState() == TransactionState.OK) {

                Transaction t = transactionIterator.next();

                printer.print(t);
                System.out.println(print);
                print.setLength(0);

            }

            if (transactionIterator.nextTransactionState() == TransactionState.CORRUPTION) {
                System.err.println("Data corruption");
                exit(1);
            } else if (transactionIterator.nextTransactionState() == TransactionState.INCOMPLETE) {
                System.err.println("Next transaction partial");
                exit(1);
            }

            exit(0);

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
