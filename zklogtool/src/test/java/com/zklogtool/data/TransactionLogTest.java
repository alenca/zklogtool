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

import com.zklogtool.reader.CRCValidationException;
import com.zklogtool.reader.IncompleteTransactionException;
import com.zklogtool.reader.TransactionLogFileReader;
import com.zklogtool.reader.TransactionLogReaderFactory;
import com.zklogtool.test.UnitTests;
import java.io.File;
import org.apache.zookeeper.txn.TxnHeader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


@Category({UnitTests.class})
public class TransactionLogTest {

    @Mock
    TransactionLogReaderFactory factoryMock;
    @Mock
    TransactionLogFileReader reader1, reader2, reader3, reader4;
    @Mock
    TransactionLogFileList transactionLogFileList;
    
    File reader1File, reader2File, reader3File, reader4File;
    
    TransactionLog transactionLogFile;

    @Before
    public void setUp() throws Exception {
        
        initMocks(this);
        
        when(reader1.getNextTransaction())
                .thenReturn(new Transaction(new TxnHeader(0,0,1,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,2,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,3,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,4,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,5,0,0),null))
                .thenReturn(null);
           
        when(reader2.getNextTransaction())
                .thenReturn(new Transaction(new TxnHeader(0,0,6,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,7,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,8,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,9,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,10,0,0),null))
                .thenReturn(null);
        
        when(reader3.getNextTransaction())
                .thenReturn(new Transaction(new TxnHeader(0,0,11,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,12,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,13,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,14,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,15,0,0),null))
                .thenReturn(null);
        
         when(reader4.getNextTransaction())
                .thenReturn(new Transaction(new TxnHeader(0,0,16,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,17,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,18,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,19,0,0),null))
                .thenReturn(new Transaction(new TxnHeader(0,0,20,0,0),null))
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(null)
                .thenThrow(new IncompleteTransactionException(""))
                .thenThrow(new IncompleteTransactionException(""))
                .thenThrow(new IncompleteTransactionException(""))
                .thenReturn(new Transaction(new TxnHeader(0,0,21,0,0),null))
                .thenReturn(null)
                .thenThrow(new IncompleteTransactionException(""))
                .thenThrow(new CRCValidationException(""))
                .thenThrow(new CRCValidationException(""))
                .thenThrow(new CRCValidationException(""))
                .thenThrow(new CRCValidationException(""));
        
        reader1File = new File("reader1File");
        reader2File = new File("reader2File");
        reader3File = new File("reader3File");
        reader4File = new File("reader4File");
        
        when(reader1.getTransactionLogFile()).thenReturn(reader1File);
        when(reader2.getTransactionLogFile()).thenReturn(reader2File);
        when(reader3.getTransactionLogFile()).thenReturn(reader3File);
        when(reader4.getTransactionLogFile()).thenReturn(reader4File);
        
        when(factoryMock.getReader(reader1File)).thenReturn(reader1);
        when(factoryMock.getReader(reader2File)).thenReturn(reader2);
        when(factoryMock.getReader(reader3File)).thenReturn(reader3);
        when(factoryMock.getReader(reader4File)).thenReturn(reader4);
        
        when(transactionLogFileList.getFirstTransactionLog()).thenReturn(reader1File);
        when(transactionLogFileList.getNextTransactionLog(reader1File)).thenReturn(reader2File);
        when(transactionLogFileList.getNextTransactionLog(reader2File)).thenReturn(reader3File);
        when(transactionLogFileList.getNextTransactionLog(reader3File))
                .thenThrow(new NoFileException(""))
                .thenThrow(new NoFileException(""))
                .thenReturn(reader4File);
        
        when(transactionLogFileList.getNextTransactionLog(reader4File))
                .thenThrow(new NoFileException(""));
       
        
        transactionLogFile = new TransactionLog(transactionLogFileList,factoryMock);
    }

    @Test
    public void testIterableInterface() throws Exception {
        
        int count = 0;
        
        for(Transaction t : transactionLogFile){   

            count++;    
            
        }
        
        assertEquals(15,count); 

    }
    
    
    @Test
    public void transactionStateTest() throws Exception{
    
          TransactionIterator iterator = transactionLogFile.iterator();
          
          assertTrue(iterator.hasNext());
          assertTrue(iterator.nextTransactionState() == TransactionState.OK);
          assertEquals(1,iterator.next().getTxnHeader().getZxid());
          while(iterator.next().getTxnHeader().getZxid()!=15){
              assertTrue(iterator.hasNext());
              assertTrue(iterator.nextTransactionState() == TransactionState.OK);
          }
          
          while(!iterator.hasNext()){}
          
          
          assertTrue(iterator.hasNext());
          assertTrue(iterator.nextTransactionState() == TransactionState.OK);
          assertEquals(16,iterator.next().getTxnHeader().getZxid());

          do{             
               iterator.next();
          }
          while(iterator.hasNext());
    
          assertFalse(iterator.hasNext());
          while(!iterator.hasNext()){}
          assertEquals(21,iterator.next().getTxnHeader().getZxid());
          assertTrue(iterator.nextTransactionState()==TransactionState.INCOMPLETE);
          assertTrue(iterator.nextTransactionState()==TransactionState.CORRUPTION);
          
    }
     
}
