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

import com.zklogtool.data.Transaction;
import com.zklogtool.printer.TransactionPrinter;
import com.zklogtool.printer.UnicodeDecoder;
import com.zklogtool.test.UnitTests;
import java.io.File;
import java.io.IOException;
import static java.lang.System.lineSeparator;
import java.net.URL;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({UnitTests.class})
public class TransactionLogFileReaderTest {
    
    Logger logger = getLogger(TransactionLogFileReaderTest.class);
    
    File txnLog1;

    @Before
    public void setUp() {

        URL url = this.getClass().getResource("/dataDir1/version-2/log.46");
        txnLog1 = new File(url.getFile());

    }

    @Test
    public void checkTestSetup() {

        assertTrue(txnLog1.isFile());

    }

    @Test
    public void getNextTransactionTest() {

        Transaction t;
        int counter = 0;
        
        StringBuilder transactionPrint = new StringBuilder();
        TransactionPrinter printer = new TransactionPrinter(transactionPrint,new UnicodeDecoder());    

        try {
            TransactionLogFileReader reader = new TransactionLogFileReader(txnLog1);
            
            do {

                t = reader.getNextTransaction();
                if (t != null) {
                    //logger.debug("Transaction cxid = " + t.getTxnHeader().getCxid());
                    
                    printer.print(t);
                    transactionPrint.append(lineSeparator()).append(lineSeparator());
                    
                    counter++;
                }
            } while (t != null);

            assertEquals(66, counter);
            
            logger.debug(transactionPrint);
            

        } catch (IOException ex) {
            fail();
        }

    }

    @Test
    public void resetTest() {

        Transaction t;
        long firstZxid;
        
        try {

            TransactionLogFileReader reader = new TransactionLogFileReader(txnLog1);
            
            t = reader.getNextTransaction();
            firstZxid = t.getTxnHeader().getZxid();

            reader.getNextTransaction();
            reader.getNextTransaction();
            reader.getNextTransaction();
            reader.getNextTransaction();
            reader.getNextTransaction();

            reader.reset();
            assertEquals(firstZxid, reader.getNextTransaction().getTxnHeader().getZxid());

        } catch (IOException ex) {
            fail();
        }

    }


}
