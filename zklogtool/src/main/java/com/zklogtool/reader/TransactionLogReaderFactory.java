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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Factory that makes <code>TransactionLogFileReader</code>s. Factory pattern is
 * used so it can be mocked and injected in tests.
 */
public class TransactionLogReaderFactory {

    public TransactionLogFileReader getReader(File transactionLog) throws FileNotFoundException, IOException {

        return new TransactionLogFileReader(transactionLog);

    }

}
