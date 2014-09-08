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

import com.zklogtool.test.UnitTests;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import static org.junit.rules.ExpectedException.none;

@Category({UnitTests.class})
public class TransactionLogFileReaderCorruptionTest {
    
    File txnLog1; //Transaction delimiter byte B replaced with 0
    File txnLog2; //Byte changed in transaction body
    File txnLog3; //Transaction delimiter byte wrong
    File txnLog4; //LEN field wrong
    File txnLog5; //EOF before transaction end 
        
    @Rule
    public ExpectedException thrown = none();

    @Before
    public void setUp() throws IOException {

        URL url = this.getClass().getResource("/transactionLogs/corruptedLog.46.1");
        txnLog1 = new File(url.getFile());
        
        url = this.getClass().getResource("/transactionLogs/corruptedLog.46.2");
        txnLog2 = new File(url.getFile());
        
        url = this.getClass().getResource("/transactionLogs/corruptedLog.46.3");
        txnLog3 = new File(url.getFile());
        
        url = this.getClass().getResource("/transactionLogs/corruptedLog.46.4");
        txnLog4 = new File(url.getFile());
        
        url = this.getClass().getResource("/transactionLogs/corruptedLog.46.5");
        txnLog5 = new File(url.getFile());

    }

    @Test
    public void checkTestSetup() {

        assertTrue(txnLog1.isFile());
        assertTrue(txnLog2.isFile());
        assertTrue(txnLog3.isFile());
        assertTrue(txnLog4.isFile());
        assertTrue(txnLog5.isFile());

    }

    @Test
    public void getNextTransactionTest1() throws IOException {
   
            thrown.expect(IncompleteTransactionException.class);
            thrown.expectMessage("Transaction delimiter byte not set");
        
            TransactionLogFileReader reader = new TransactionLogFileReader(txnLog1);
        
            for (int i = 0; i< 100; i++){
                reader.getNextTransaction();
            } 

    }
 
    @Test
    public void getNextTransactionTest2() throws IOException {
        
            thrown.expect(CRCValidationException.class);
            thrown.expectMessage("Transaction CRC validation failed");
   
            TransactionLogFileReader reader = new TransactionLogFileReader(txnLog2);
        
            for (int i = 0; i< 100; i++){
                reader.getNextTransaction();
            } 

    }
    
    @Test
    public void getNextTransactionTest3() throws IOException {
   
            thrown.expect(CRCValidationException.class);
            thrown.expectMessage("Transaction delimiter byte wrong");
            
            TransactionLogFileReader reader = new TransactionLogFileReader(txnLog3);
        
            for (int i = 0; i< 100; i++){
                reader.getNextTransaction();
            } 

    }
    
    
    @Test
    public void getNextTransactionTest4() throws IOException {
        
            thrown.expect(CRCValidationException.class);
            thrown.expectMessage("Transaction delimiter byte wrong");
   
            TransactionLogFileReader reader = new TransactionLogFileReader(txnLog4);
        
            for (int i = 0; i< 100; i++){
                reader.getNextTransaction();
            } 

    }
    
    @Test
    public void getNextTransactionTest5() throws IOException {
        
            thrown.expect(IncompleteTransactionException.class);
            thrown.expectMessage("Problem with reading file before transaction end");
   
            TransactionLogFileReader reader = new TransactionLogFileReader(txnLog5);
        
            for (int i = 0; i< 100; i++){
                reader.getNextTransaction();
            } 

    }

}
