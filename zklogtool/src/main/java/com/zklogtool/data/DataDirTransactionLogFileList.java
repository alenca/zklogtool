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
package com.zklogtool.data;

import com.zklogtool.util.DataDirHelper;
import java.io.File;
import java.util.List;

/**
 * Implementation of <code>TransactionLogFileList</code> interface for Zookeeper
 * data directory. This implementation depends on file names. Files must be
 * named "log.xxxx" where xxxx is hexadecimal number. That number must represent
 * zxid of first transaction written to that file. Zookeeper uses this naming
 * convention on default so this implementation works for untampered Zookeeper
 * data log directory.
 *
 */
public class DataDirTransactionLogFileList implements TransactionLogFileList {

    private File dataDir;
    private DataDirHelper dataDirHelper;

    /**
     *
     * @param dataDir Directory where transaction log files are stored.
     */
    public DataDirTransactionLogFileList(File dataDir) {
        this.dataDir = dataDir;
        dataDirHelper = new DataDirHelper(dataDir, null);
    }

    /**
     * Returns chronologically first transaction log file from directory
     * injected in constructor. That is file that has lowest zxid in filename.
     *
     * @return Chronologically first transaction log file from directory
     * injected in constructor.
     * @throws NoFileException Thrown if there are no transaction log files at
     * all in directory injected in constructor.
     */
    @Override
    public File getFirstTransactionLog() throws NoFileException {

        List<File> transactionLogs = dataDirHelper.getSortedLogList();

        if (!transactionLogs.isEmpty()) {
            return transactionLogs.get(0);
        } else {
            throw new NoFileException("No transaction log found in " + dataDir.getAbsolutePath());
        }

    }

    /**
     * Returns transaction log file that chronologically succeeds
     * <code>previousTransactionLog</code>. That is file with next lowest zxid
     * number in name.
     *
     * @param previousTransactionLog Transaction log file that chronologically
     * precedes transaction log that is to be returned.
     * @return Transaction log file that chronologically succeeds
     * <code>previousTransactionLog</code>.
     * @throws NoFileException Thrown if there is no transaction log file with
     * higher zxid number in directory injected in constructor.
     */
    @Override
    public File getNextTransactionLog(File previousTransactionLog) throws NoFileException {

        List<File> transactionLogs = dataDirHelper.getSortedLogList();

        for (int i = 0; i < transactionLogs.size() - 1; i++) {

            if (previousTransactionLog.getAbsolutePath().contentEquals(transactionLogs.get(i).getAbsolutePath())) {
                return transactionLogs.get(i + 1);
            }

        }

        throw new NoFileException("No transaction log found in " + dataDir.getAbsolutePath() 
                + "that succeeds" + previousTransactionLog.toString());

    }

}
