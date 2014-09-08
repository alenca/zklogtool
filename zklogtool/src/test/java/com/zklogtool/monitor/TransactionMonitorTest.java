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
package com.zklogtool.monitor;

import com.zklogtool.data.Transaction;
import com.zklogtool.data.TransactionIterator;
import com.zklogtool.data.TransactionLog;
import com.zklogtool.data.TransactionState;
import com.zklogtool.test.UnitTests;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import org.apache.zookeeper.txn.TxnHeader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@Category({UnitTests.class})
public class TransactionMonitorTest {
    
    Logger logger = getLogger(TransactionMonitorTest.class);

    TransactionMonitor transactionMonitor;

    @Mock
    TransactionLog transactionLog;
    @Mock
    TransactionIterator transactionIterator;

    @Before
    public void setUp() {

        initMocks(this);

        when(transactionIterator.next())
                .thenReturn(new Transaction(new TxnHeader(0, 0, 1, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 2, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 3, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 4, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 5, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 6, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 7, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 8, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 9, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 10, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 11, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 12, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 13, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 14, 0, 0), null))
                .thenReturn(new Transaction(new TxnHeader(0, 0, 15, 0, 0), null));

        when(transactionLog.iterator())
                .thenReturn(transactionIterator);

        transactionMonitor = new TransactionMonitor(transactionLog);

    }

    @Test(timeout = 3000)
    public void testStartAtFirstTransaction() throws InterruptedException {

        when(transactionIterator.nextTransactionState())
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.EMPTY)
                .thenReturn(TransactionState.INCOMPLETE)
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.EMPTY);

        final CountDownLatch endSignal = new CountDownLatch(7);

        transactionMonitor.addListener(new TransactionListener() {

            int nextTransaction = 1;

            @Override
            public void onTransaction(Transaction t) {

                assertEquals(nextTransaction, t.getTxnHeader().getZxid());
                nextTransaction++;
                endSignal.countDown();

                logger.debug("test 1:" + nextTransaction);

            }

            @Override
            public void onPartialTransaction() {
                logger.debug("Incomplete");

                assertEquals(5, nextTransaction);
            }

            @Override
            public void onCorruption() {
                fail();
            }

        });

        transactionMonitor.startAtFirstTransaction();
        endSignal.await();

    }

    @Test(timeout=3000)
    public void testStartAtLastTransaction() throws InterruptedException {

        when(transactionIterator.nextTransactionState())
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.EMPTY)
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.OK)
                .thenReturn(TransactionState.EMPTY);

        final CountDownLatch endSignal = new CountDownLatch(3);

        transactionMonitor.addListener(new TransactionListener() {

            int nextTransaction = 5;

            @Override
            public void onTransaction(Transaction t) {

                assertEquals(nextTransaction, t.getTxnHeader().getZxid());
                nextTransaction++;
                endSignal.countDown();

                logger.debug("test 2:" + nextTransaction);

            }

            @Override
            public void onPartialTransaction() {
                fail();
            }

            @Override
            public void onCorruption() {
                fail();
            }

        });

        transactionMonitor.startAtLastTransaction();
        endSignal.await();

    }

}
