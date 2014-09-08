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

import java.io.File;

/**
 * Interface that provides transaction log files. Used by
 * <code>TransactionLog</code>. Default implementation is
 * <code>DataDirTransactionLogFileList</code>. Useful to implement if
 * transaction log files are remotely fetched or do not satisfy standard
 * Zookeeper naming scheme.
 *
 */
public interface TransactionLogFileList {

    /**
     *
     * @return First transaction log file to be processed.
     * @throws NoFileException Thrown if file is not (yet) accessible.
     */
    File getFirstTransactionLog() throws NoFileException;

    /**
     *
     * @param previousTransactionLog Transaction log file that precedes
     * file that is to be returned. 
     * @return Transaction log file that succeeds
     * <code>previousTransactionLog</code> transaction log file.
     * @throws NoFileException Thrown if file is not (yet) accessible.
     */
    File getNextTransactionLog(File previousTransactionLog) throws NoFileException;

}
