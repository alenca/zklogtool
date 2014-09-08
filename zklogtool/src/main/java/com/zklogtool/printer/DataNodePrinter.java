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

import com.zklogtool.data.DataState;
import com.zklogtool.util.Util;
import static com.zklogtool.util.Util.getACLString;
import static com.zklogtool.util.Util.readData;
import static java.lang.System.lineSeparator;
import java.util.Date;
import java.util.List;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.server.DataNode;

/**
 * <code>DataNodePrinter</code> is used for appending <code>DataNode</code>
 * information to <code>StringBuilder</code>.
 *
 * <code>DataNodePrinter</code> appends following fields form
 * <code>DataNode</code> object:
 * <br>
 * <ul>
 * <li><code>long czxid</code></li>
 * <li><code>long mzxid</code></li>
 * <li><code>long ctime</code></li>
 * <li><code>long mtime</code></li>
 * <li><code>int version</code></li>
 * <li><code>int cversion</code></li>
 * <li><code>int aversion</code></li>
 * <li><code>long ephermalOwner</code></li>
 * <li><code>List&lt;ACL&gt; ACL</code></li>
 * <li>byte[] data - converted to String using <code>DataDecoder</code>s
 * injected in constructor</li>
 * </ul>
 */
public class DataNodePrinter {

    StringBuilder sb;
    DataDecoder dd;

    int indentation;

    /**
     *
     * @param sb <code>StringBuilder</code> to which information is appended.
     * @param dd Decoder used to convert DataNode data byte array to
     * <code>String</code>.
     */
    public DataNodePrinter(StringBuilder sb, DataDecoder dd) {

        this.sb = sb;
        this.dd = dd;

        indentation = 0;
    }

    private void println(String s) {
        for (int i = 0; i < indentation; i++) {
            sb.append("\t");
        }
        sb.append(s).append(lineSeparator());
    }

    /**
     *
     * @param dataNode <code>DataNode</code> to print information from.
     * @param dataState <code>DataState</code> is needed because it holds ACL.
     */
    public void printDataNode(DataNode dataNode, DataState dataState) {

        println("cZxid:\t\t" + Util.longToHexString(dataNode.stat.getCzxid()));
        println("mZxid:\t\t" + Util.longToHexString(dataNode.stat.getMzxid()));
        println("ctime:\t\t" + new Date(dataNode.stat.getCtime()));
        println("mtime:\t\t" + new Date(dataNode.stat.getMtime()));
        println("version:\t" + dataNode.stat.getVersion());
        println("cversion:\t" + dataNode.stat.getCversion());
        println("aversion:\t" + dataNode.stat.getAversion());
        println("ephemeralOwner:\t" + Util.longToHexString(dataNode.stat.getEphemeralOwner()));
        printACL(dataState.getACL(dataNode));

        byte[] data = readData(dataNode);

        if (data != null) {
            println("data:\t\t" + dd.decode(data));
        } else {
            println("data:\t\tnull");
        }

    }

    private void printACL(List<ACL> acl) {

        println("ACL:");

        indentation += 2;

        //special paths have no ACL
        if (acl != null) {
            for (ACL a : acl) {

                String perms = getACLString(a.getPerms());
                String id = a.getId().getId();
                String scheme = a.getId().getScheme();

                println(scheme + ":" + id + "  " + perms);
            }
        } else {
            println("null");
        }

        indentation -= 2;

    }

}
