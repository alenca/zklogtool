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

import com.zklogtool.test.UnitTests;
import java.io.File;
import java.net.URL;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import static org.junit.rules.ExpectedException.none;


@Category({UnitTests.class})
public class DataDirTransactionLogFileListTest {
    
    File dataDir;
    DataDirTransactionLogFileList dataDirTransactionLogFileList;
 
    @Rule
    public ExpectedException thrown = none();

    @Before
    public void setUp() {

        URL url = this.getClass().getResource("/dataDir1/version-2/");
        dataDir = new File(url.getFile());
        
        dataDirTransactionLogFileList = new DataDirTransactionLogFileList(dataDir);

    }

    @Test
    public void testSetup() {

        assertTrue(dataDir.isDirectory());

    }

    
    @Test
    public void test() throws NoFileException{
    
        File log = dataDirTransactionLogFileList.getFirstTransactionLog();
        
        assertTrue(log.getName().contentEquals("log.1"));
        
        log = dataDirTransactionLogFileList.getNextTransactionLog(log);
        
        assertTrue(log.getName().contentEquals("log.46"));
        
        log = dataDirTransactionLogFileList.getNextTransactionLog(log);
        
        assertTrue(log.getName().contentEquals("log.88"));
        
        log = dataDirTransactionLogFileList.getNextTransactionLog(log);
        
        assertTrue(log.getName().contentEquals("log.b6"));
        
        log = dataDirTransactionLogFileList.getNextTransactionLog(log);
        
        assertTrue(log.getName().contentEquals("log.e9"));
        
        thrown.expect(NoFileException.class);
           
        dataDirTransactionLogFileList.getNextTransactionLog(log);
        
    }
    
    
}
