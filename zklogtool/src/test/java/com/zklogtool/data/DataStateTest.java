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

import com.zklogtool.reader.SnapshotFileReader;
import com.zklogtool.reader.TransactionLogReaderFactory;
import com.zklogtool.test.IntegrationTests;
import java.io.File;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({IntegrationTests.class})
public class DataStateTest {
  
    File dataDir;
    File snap1;
    
    long TS1;
    
    DataState dataState;
    
    TransactionLog transactionLog;

    @Before
    public void setUp() throws Exception {

        URL url = this.getClass().getResource("/dataDir1/version-2/");
        dataDir = new File(url.getFile());
        
        url = this.getClass().getResource("/dataDir1/version-2/snapshot.44");
        snap1 = new File(url.getFile());
        
        DataDirTransactionLogFileList s = new DataDirTransactionLogFileList(dataDir);  
        
        transactionLog = new TransactionLog(s, new TransactionLogReaderFactory());
        
        SnapshotFileReader snapshotFileReader = new SnapshotFileReader(snap1,TS1);
        
        dataState = snapshotFileReader.restoreDataState(transactionLog.iterator());


    }

    @Test
    public void testSetup() {

        assertTrue(dataDir.isDirectory());
        assertTrue(snap1.isFile());

    }

    
    @Test
    public void test() throws NoFileException, Exception{
        
        
        long lastzxid = dataState.getLastZxid();
        
        TransactionIterator iterator = transactionLog.iterator();
        
        Transaction t;
        
        do{
        
            t = iterator.next();
        
        }while(t.getTxnHeader().getZxid()<lastzxid);
        
        
        dataState.processTransaction(iterator.next());
        dataState.processTransaction(iterator.next());
        dataState.processTransaction(iterator.next());
        dataState.processTransaction(iterator.next());
        dataState.processTransaction(iterator.next());
        
        assertEquals(lastzxid+5,dataState.getLastZxid());
        
        
    }
    
}
