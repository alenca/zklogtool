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

import org.apache.jute.Record;
import org.apache.zookeeper.txn.TxnHeader;

/**
 * This class represents Zookeeper transaction. Transaction is unit of change.
 * It can be applied to <code>DataState</code>. <code>Transaction</code> is
 * encapsulation of Zookeeper internal <code>TxnHeader</code> and
 * <code>Record</code> classes. Those two get serialized in transaction log
 * files.
 *
 * <code>TxnHeader</code> has following info available through public getters:
 * <br>
 * <ul>
 * <li><code>long clientId</code></li>
 * <li><code>int cxid</code></li>
 * <li><code>long zxid</code></li>
 * <li><code>long time</code></li>
 * <li><code>int type</code></li>
 * </ul>
 * <br>
 * To access <code>Record</code> data, it should be casted to one of its subclasses based on
 * transaction type. Subclasses have use specific getters. Use following table
 * to determine subclass:
 * <br>
 * <table>
 * <tr>
 * <th>
 * Transaction type
 * </th>
 * <th>
 * Record subclass
 * </th>
 * </tr>
 * <tr>
 * <td>
 * org.apache.zookeeper.ZooDefs.OpCode.create
 * </td>
 * <td>
 * org.apache.zookeeper.txn.CreateTxn
 * </td>
 * </tr>
 * <tr>
 * <td>
 * org.apache.zookeeper.ZooDefs.OpCode.delete
 * </td>
 * <td>
 * org.apache.zookeeper.txn.DeleteTxn
 * </td>
 * </tr>
 * <tr>
 * <td>
 * org.apache.zookeeper.ZooDefs.OpCode.setData
 * </td>
 * <td>
 * org.apache.zookeeper.txn.SetDataTxn
 * </td>
 * </tr>
 * <tr>
 * <td>
 * org.apache.zookeeper.ZooDefs.OpCode.setACL
 * </td>
 * <td>
 * org.apache.zookeeper.txn.SetACLTxn
 * </td>
 * </tr>
 * <tr>
 * <td>
 * org.apache.zookeeper.ZooDefs.OpCode.check
 * </td>
 * <td>
 * org.apache.zookeeper.txn.CheckVersionTxn
 * </td>
 * </tr>
 * <tr>
 * <td>
 * org.apache.zookeeper.ZooDefs.OpCode.multi
 * </td>
 * <td>
 * org.apache.zookeeper.txn.MultiTxn
 * </td>
 * </tr>
 * <tr>
 * <td>
 * org.apache.zookeeper.ZooDefs.OpCode.createSession
 * </td>
 * <td>
 * org.apache.zookeeper.txn.CreateSessionTxn
 * </td>
 * </tr>
 * <tr>
 * <td>
 * org.apache.zookeeper.ZooDefs.OpCode.closeSession
 * </td>
 * <td>
 * org.apache.zookeeper.txn.CreateSessionTxn
 * </td>
 * </tr>
 * <tr>
 * <td>
 * org.apache.zookeeper.ZooDefs.OpCode.error
 * </td>
 * <td>
 * org.apache.zookeeper.txn.ErrorTxn
 * </td>
 * </tr>
 * </table>
 *
 */
public class Transaction {

    private TxnHeader txnHeader;
    private Record txnRecord;

    public Transaction(TxnHeader txnHeader, Record txnRecord) {
        super();
        this.txnHeader = txnHeader;
        this.txnRecord = txnRecord;
    }

    public TxnHeader getTxnHeader() {
        return txnHeader;
    }

    public void setTxnHeader(TxnHeader txnHeader) {
        this.txnHeader = txnHeader;
    }

    public Record getTxnRecord() {
        return txnRecord;
    }

    public void setTxnRecord(Record txnRecord) {
        this.txnRecord = txnRecord;
    }

}
