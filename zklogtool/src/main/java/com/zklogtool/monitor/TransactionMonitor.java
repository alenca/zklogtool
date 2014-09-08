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
package com.zklogtool.monitor;

import com.zklogtool.data.Transaction;
import com.zklogtool.data.TransactionIterator;
import com.zklogtool.data.TransactionLog;
import com.zklogtool.data.TransactionState;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>TransactionMonitor</code> watches <code>TransactionLog</code> for new
 * transactions and notifies its listeners. <code>TransactionMonitor</code> runs
 * in its own thread and periodically checks <code>TransactionLog</code> for any
 * new transactions.
 * <br>
 * <code>TransactionMonitor</code> can be started in two ways:
 * <br>
 * <ul>
 * <li>notify listeners about all already written transactions and than continue
 * monitoring - <code>startAtFirstTransaction()</code></li>
 * <li>notify listeners only about transaction that were not written before
 * monitoring started - <code>startAtLastTransaction()</code></li>
 * </ul>
 *
 */
public class TransactionMonitor {

    boolean running = false;
    TransactionLog transactionLog;
    TransactionIterator iterator;

    List<TransactionListener> listeners = new ArrayList<>();

    Thread monitorThread = new Thread() {

        public void run() {

            TransactionState lastState = null;

            while (running) {

                try {

                    while (iterator.nextTransactionState() == TransactionState.OK) {

                        notifyOnTransaction(iterator.next());

                    }

                    TransactionState n = iterator.nextTransactionState();

                    if (n == TransactionState.INCOMPLETE && n != lastState) {
                        notifyOnPartialTransaction();
                    } else if (n == TransactionState.CORRUPTION && n != lastState) {
                        notifyOnCorruption();
                    }

                    lastState = n;

                    sleep(50);

                } catch (InterruptedException ex) {
                    //boring
                }
            }
        }

    };

    /**
     *
     * @param transactionLog <code>TransactionLog</code> to monitor for new
     * <code>Transaction</code>s.
     */
    public TransactionMonitor(final TransactionLog transactionLog) {

        this.transactionLog = transactionLog;

        monitorThread.setName("TransactionMonitor thread");

    }

    /**
     * Start <code>TransactionMonitor</code> in a way that it notifies listeners
     * about all already written transactions and than continues monitoring.
     *
     */
    public void startAtFirstTransaction() {

        if (!running) {

            iterator = transactionLog.iterator();

            running = true;

            monitorThread.start();

        }

    }

    /**
     * Start <code>TransactionMonitor</code> in a way that it notifies listeners
     * only about transactions that were not written before monitoring started.
     *
     */
    public void startAtLastTransaction() {

        if (!running) {

            iterator = transactionLog.iterator();

            while (iterator.nextTransactionState() == TransactionState.OK) {

                iterator.next();

            }

            running = true;

            monitorThread.start();
        }

    }

    /**
     * Stops monitoring thread.
     *
     */
    public void stop() {

        running = false;

    }

    /**
     *
     * @param l <code>TransactionListener</code> to be added to listeners list.
     */
    public void addListener(TransactionListener l) {

        listeners.add(l);

    }

    /**
     *
     * @param l <code>TransactionListener</code> to be removed from listeners
     * list.
     */
    public void removeListener(TransactionListener l) {

        listeners.remove(l);

    }

    private void notifyOnTransaction(Transaction t) {

        //listener could change transaction, not safe, maybe send a copy
        for (TransactionListener l : listeners) {
            l.onTransaction(t);
        }
    }

    private void notifyOnPartialTransaction() {

        for (TransactionListener l : listeners) {
            l.onPartialTransaction();
        }

    }

    private void notifyOnCorruption() {

        for (TransactionListener l : listeners) {
            l.onCorruption();
        }

    }

}
