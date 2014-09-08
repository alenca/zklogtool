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
package com.zklogtool.printer;

import com.zklogtool.data.Transaction;
import com.zklogtool.util.Util;
import static com.zklogtool.util.Util.getACLString;
import java.io.IOException;
import static java.lang.System.lineSeparator;
import java.nio.ByteBuffer;
import static java.nio.ByteBuffer.wrap;
import java.util.Date;
import java.util.List;
import org.apache.jute.Record;
import org.apache.zookeeper.ZooDefs.OpCode;
import org.apache.zookeeper.data.ACL;
import static org.apache.zookeeper.server.ByteBufferInputStream.byteBuffer2Record;
import org.apache.zookeeper.txn.CheckVersionTxn;
import org.apache.zookeeper.txn.CreateSessionTxn;
import org.apache.zookeeper.txn.CreateTxn;
import org.apache.zookeeper.txn.DeleteTxn;
import org.apache.zookeeper.txn.ErrorTxn;
import org.apache.zookeeper.txn.MultiTxn;
import org.apache.zookeeper.txn.SetACLTxn;
import org.apache.zookeeper.txn.SetDataTxn;
import org.apache.zookeeper.txn.Txn;
import org.apache.zookeeper.txn.TxnHeader;

/**
 *
 * <code>TransactionPrinter</code> is used for appending
 * <code>Transaction</code> information to <code>StringBuilder</code>. Printed
 * fields depend on transaction type because different transaction types hold
 * different kind of data. It it possible to set indentation for each print in
 * order to make data more readable.
 *
 */
public class TransactionPrinter {

    StringBuilder sb;
    DataDecoder dd;

    int indentation;

    /**
     *
     * @param sb <code>StringBuilder</code> to which information is be appended
     * @param dd Decoder used to convert transaction data byte array to
     * <code>String</code>. Transaction types that hold data are create and
     * setData.
     */
    public TransactionPrinter(StringBuilder sb, DataDecoder dd) {
        this.sb = sb;
        this.dd = dd;

        indentation = 0;
    }

    public int getIndentation() {
        return indentation;
    }

    /**
     * Indentation if number of tab characters appended in front of actual data.
     *
     * @param indentation Positive integer that represents indentation.
     */
    public void setIndentation(int indentation) {
        this.indentation = indentation;
    }

    private void println(String s) {
        for (int i = 0; i < indentation; i++) {
            sb.append("\t");
        }
        sb.append(s).append(lineSeparator());
    }

    private void printOperation(String operation) {
        println("Operation:\t" + operation);
    }

    private void printPath(String path) {
        println("Path:\t\t" + path);
    }

    private void printEphermal(boolean ephermal) {
        println("Ephermal:\t" + ephermal);
    }

    private void printParentCVersion(int parentCVersion) {
        println("ParentCVersion:\t" + parentCVersion);
    }

    private void printVersion(int version) {
        println("Version:\t" + version);
    }

    private void printTimeout(int timeOut) {
        println("Timeout:\t" + timeOut);
    }

    private void printError(int error) {
        println("Error:\t\t" + error);
    }

    private void printHeader(TxnHeader txnHeader) {
        println("Zxid:\t\t" + Util.longToHexString(txnHeader.getZxid()));
        println("Cxid:\t\t" + Util.longToHexString(txnHeader.getCxid()));
        println("Client id:\t" + Util.longToHexString(txnHeader.getClientId()));
        println("Time:\t\t" + new Date(txnHeader.getTime()));
    }

    private void printRecord(Record r, int type) {

        switch (type) {

            case OpCode.create:

                //check if you should use CreateTxnV0 instead
                CreateTxn createTxn = (CreateTxn) r;
                printOperation("create");
                printPath(createTxn.getPath());
                printEphermal(createTxn.getEphemeral());
                printParentCVersion(createTxn.getParentCVersion());
                printACL(createTxn.getAcl());
                printData(createTxn.getData());
                break;

            case OpCode.delete:

                DeleteTxn deleteTxn = (DeleteTxn) r;
                printOperation("delete");
                printPath(deleteTxn.getPath());
                break;

            case OpCode.setData:

                SetDataTxn setDataTxn = (SetDataTxn) r;
                printOperation("setData");
                printPath(setDataTxn.getPath());
                printData(setDataTxn.getData());
                printVersion(setDataTxn.getVersion());
                break;

            case OpCode.setACL:
                SetACLTxn setACLTxn = (SetACLTxn) r;
                printOperation("setACL");
                printPath(setACLTxn.getPath());
                printACL(setACLTxn.getAcl());
                printVersion(setACLTxn.getVersion());
                break;

            case OpCode.check:
                CheckVersionTxn checkVersionTxn = (CheckVersionTxn) r;
                printOperation("check");
                printPath(checkVersionTxn.getPath());
                printVersion(checkVersionTxn.getVersion());
                break;

            case OpCode.multi:
                MultiTxn multiTxn = (MultiTxn) r;
                printOperation("multi");
                indentation++;

                for (Txn txn : multiTxn.getTxns()) {
                    ByteBuffer bb = wrap(txn.getData());
                    Record record = null;
                    switch (txn.getType()) {
                        case OpCode.create:
                            record = new CreateTxn();
                            break;
                        case OpCode.delete:
                            record = new DeleteTxn();
                            break;
                        case OpCode.setData:
                            record = new SetDataTxn();
                            break;
                        case OpCode.error:
                            record = new ErrorTxn();
                            break;
                        case OpCode.check:
                            record = new CheckVersionTxn();
                            break;
                        default:
                            break;
                    }

                    try {

                        byteBuffer2Record(bb, record);
                        printRecord(record, txn.getType());

                    } catch (IOException ex) {
                        //this should never happen
                    }
                }

                indentation--;

                break;

            //create and close session use the same txn
            case OpCode.createSession:
                CreateSessionTxn createSessionTxn = (CreateSessionTxn) r;
                printOperation("createSession");
                printTimeout(createSessionTxn.getTimeOut());
                break;

            case OpCode.closeSession:
                CreateSessionTxn closeSessionTxn = (CreateSessionTxn) r;
                printOperation("closeSession");
                break;

            case OpCode.error:
                ErrorTxn errorTxn = (ErrorTxn) r;
                printOperation("error");
                printError(errorTxn.getErr());
                break;

            //there is also SetMaxChildrenTxn but not sure if needed
        }

    }

    /**
     *
     * @param t <code>Transaction</code> to print information from.
     */
    public void print(Transaction t) {

        TxnHeader h = t.getTxnHeader();
        Record r = t.getTxnRecord();

        printHeader(h);
        printRecord(r, h.getType());

    }

    private void printData(byte[] b) {

        if (b != null) {
            println("Data:\t\t" + dd.decode(b));
        } else {
            println("Data:\t\tnull");
        }

    }

    private void printACL(List<ACL> acl) {

        println("ACL:");

        indentation += 2;

        for (ACL a : acl) {

            String perms = getACLString(a.getPerms());
            String id = a.getId().getId();
            String scheme = a.getId().getScheme();

            println(scheme + ":" + id + "  " + perms);
        }

        indentation -= 2;

    }

}
