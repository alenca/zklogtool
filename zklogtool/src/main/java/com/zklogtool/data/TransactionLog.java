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
package com.zklogtool.data;

import com.zklogtool.reader.CRCValidationException;
import com.zklogtool.reader.IncompleteTransactionException;
import com.zklogtool.reader.TransactionLogFileReader;
import com.zklogtool.reader.TransactionLogReaderFactory;
import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * <code>TransactionLog</code> represents one or multiple Zookeeper transaction
 * log files. It implements <code>Iterable</code> interface.
 * <code>TransactionLog</code> can be constructed to iterate over transactions
 * written to one or multiple files using different constructors.
 * <br>
 * Zookeeper transactions are appended to last transaction log file, and from
 * time to time new file gets created. <code>TransactionLog</code> covers both
 * cases and seamlessly switches over to new file when needed.
 *
 */
public class TransactionLog implements Iterable<Transaction> {

    final TransactionLogFileList transactionLogList;
    final TransactionLogReaderFactory factory;

    /**
     * Constructs <code>TransactionLog</code> that provides iterator over
     * <code>Transactions</code> in one transaction log file
     * <code>transactionLogFile</code>.
     *
     * @param transactionLogFile Transaction log file to iterate over.
     * @param factory <code>TransactionLogReaderFactory</code> that provides
     * <code>TransactionLogFileReader</code> to be used.
     */
    public TransactionLog(final File transactionLogFile, TransactionLogReaderFactory factory) {

        this.transactionLogList = new TransactionLogFileList() {

            @Override
            public File getFirstTransactionLog() throws NoFileException {
                return transactionLogFile;
            }

            @Override
            public File getNextTransactionLog(File previousTransactionLog) throws NoFileException {
                throw new NoFileException("");
            }

        };

        this.factory = factory;

    }

    /**
     * Constructs <code>TransactionLog</code> that provides iterator over
     * <code>Transactions</code> written in multiple transaction log files.
     * Transaction log files are provided by
     * <code>transactionLogFileList</code>.
     *
     * @param transactionLogFileList Provides transaction log files to iterate
     * over.
     * @param factory <code>TransactionLogReaderFactory</code> that provides
     * <code>TransactionLogFileReader</code> to be used.
     */
    public TransactionLog(TransactionLogFileList transactionLogFileList, TransactionLogReaderFactory factory) {

        this.transactionLogList = transactionLogFileList;
        this.factory = factory;

    }

    @Override
    public TransactionIterator iterator() {

        return new TransactionLogIterator(transactionLogList, factory);
    }

    /**
     * Implements <code>TransactionIterator</code>. Remove operation is not
     * supported.
     *
     */
    public class TransactionLogIterator implements TransactionIterator {

        private TransactionLogFileReader reader;
        private TransactionLogFileList transactionLogList;

        private Transaction t;
        private TransactionState s;

        private TransactionLogIterator(TransactionLogFileList transactionLogFileList, TransactionLogReaderFactory factory) {

            this.transactionLogList = transactionLogFileList;

            loadNextTransaction();

        }

        @Override
        public boolean hasNext() {

            if (nextTransactionState() == TransactionState.OK) {
                return true;
            } else {
                return false;
            }

        }

        @Override
        public Transaction next() {

            if (hasNext()) {

                Transaction temp = t;

                loadNextTransaction();

                return temp;

            } else {
                throw new NoSuchElementException("No such element");
            }

        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }

        /**
         *
         * @return TransactionState of the next <code>Transaction</code> in
         * iteration.
         */
        @Override
        public TransactionState nextTransactionState() {

            if (s != TransactionState.OK) {
                loadNextTransaction();
            }

            return s;
        }

        private void loadNextTransaction() {

            try {

                if (reader == null) {

                    reader = factory.getReader(transactionLogList.getFirstTransactionLog());

                }

                t = reader.getNextTransaction();

                if (t != null) {
                    s = TransactionState.OK;
                } else {
                    s = TransactionState.EMPTY;

                    try {

                        reader = factory.getReader(transactionLogList.getNextTransactionLog(reader.getTransactionLogFile()));

                    } catch (NoFileException ex) {
                        // this is ok, there is no next file so default behaviour is to stick with current file
                    }
                }

            } catch (IncompleteTransactionException ex) {

                s = TransactionState.INCOMPLETE;

            } catch (CRCValidationException ex) {

                s = TransactionState.CORRUPTION;

            } catch (IOException ex) {

                s = TransactionState.CORRUPTION;

            } catch (NoFileException ex) {

                s = TransactionState.EMPTY;

            }

        }

    }

}
