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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jute.Record;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.server.DataNode;
import org.apache.zookeeper.server.DataTree;
import org.apache.zookeeper.txn.CreateSessionTxn;
import org.apache.zookeeper.txn.TxnHeader;

/**
 * Model that holds entire Zookeeper data state. It encapsulates of
 * <code>DataTree</code>, <code>Map</code> of session identifiers and
 * <code>boolean fuzzy</code> that has <code>true</code> value if
 * <code>DataState</code> is in fuzzy state. This class should never be manually
 * constructed. Best way to get instance is using
 * <code>SnapshotFileReader</code>.
 *
 */
public final class DataState {

    private DataTree dt;
    private Map<Long, Integer> sessions;
    private boolean fuzzy;

    /**
     *
     * @param dt Zookeeper <code>DataTree</code> structure.
     * @param sessions <code>Map</code> of session identifiers and session
     * timeouts.
     * @param lastZxid Zxid of last committed transaction.
     */
    public DataState(DataTree dt, Map<Long, Integer> sessions, long lastZxid) {

        this.dt = dt;
        this.sessions = sessions;
        setLastZxid(lastZxid);
    }

    /**
     *
     * @return <code>Map</code> of session identifiers and session timeouts.
     */
    public Map<Long, Integer> getSessions() {
        return sessions;
    }

    /**
     *
     * @param sessions <code>Map</code> of session identifiers and session
     * timeouts.
     */
    public void setSessions(Map<Long, Integer> sessions) {
        this.sessions = sessions;
    }

    /**
     *
     * @return Zxid of last committed transaction.
     */
    public long getLastZxid() {
        //return lastZxid;
        return dt.lastProcessedZxid;
    }

    /**
     *
     * @param lastZxid Zxid of last committed transaction.
     */
    public void setLastZxid(long lastZxid) {
        //this.lastZxid = lastZxid;
        dt.lastProcessedZxid = lastZxid;
    }

    //this should return something meaningfull
    /**
     * Applies transaction to this <code>DataState</code>. Zxid of transaction
     * <code>t</code> is not checked, and it is possible to apply out of order
     * transactions using this method. User should take care about consistency.
     *
     * @param t Transaction to be applied.
     */
    public void processTransaction(Transaction t) {

        TxnHeader hdr = t.getTxnHeader();
        Record txn = t.getTxnRecord();

        //there should be a check for put and remove operations
        switch (hdr.getType()) {
            case ZooDefs.OpCode.createSession:
                sessions.put(hdr.getClientId(), ((CreateSessionTxn) txn).getTimeOut());
                break;
            case ZooDefs.OpCode.closeSession:
                sessions.remove(hdr.getClientId());
                break;
        }

        dt.processTxn(hdr, txn);

    }

    
    /**
     * Returns <code>Set</code> of children names of znode <code>path</code>.
     * 
     * @param path Path of parent znode.
     * @return <code>Set</code> of children names of znode <code>path</code>.
     */
    public Set<String> getChildren(String path) {

        return getNode(path).getChildren();

    }

    /**
     * 
     * @return Number of persistent znodes.
     */
    public int getNodeCount() {
        return getNodes().size();
    }

    /**
     *
     * @return Number of ephemeral znodes.
     */
    public int getEphemeralsCount() {
        return getEphemerals().size();
    }

    /**
     *
     * @return <code>Map</code> of persistent znode names and <code>DataNode</code>s.
     */
    public Map<String, DataNode> getNodes() {

        try {

            Field field = dt.getClass().getDeclaredField("nodes");
            field.setAccessible(true);
            Object value = field.get(dt);
            return (Map<String, DataNode>) value;

        } catch (Exception ex) {

            //this should never happen
            return null;

        }

    }

    /**
     *
     * @return <code>Map</code> of ephemeral znode names and <code>DataNode</code>s.
     */
    public Map<Long, HashSet<String>> getEphemerals() {

        try {

            Field field = dt.getClass().getDeclaredField("ephemerals");
            field.setAccessible(true);
            Object value = field.get(dt);

            return (Map<Long, HashSet<String>>) value;

        } catch (Exception ex) {

            //this should never happen
            return null;

        }

    }

    /**
     * Returns <code>DataNode</code> for znode <code>path</code>.
     * 
     * @param path Full name of znode.
     * @return <code>DataNode</code> structure of <code>path</code> znode.
     */
    public DataNode getNode(String path) {
        return getNodes().get(path);
    }

    /**
     * Returns Zookeeper <code>List&lt;ACL&gt;</code> of <code>dataNode</code>.
     * @param dataNode <code>DataNode</code> for which ACL list is returned.
     * @return Zookeeper <code>List\<ACL\></code> of <code>dataNode</code>.
     */
    public List<ACL> getACL(DataNode dataNode) {

        try {

            Field field = dt.getClass().getDeclaredField("longKeyMap");
            field.setAccessible(true);
            Object value = field.get(dt);
            Map<Long, List<ACL>> longKeyMap = (Map<Long, List<ACL>>) value;

            Field field2 = DataNode.class.getDeclaredField("acl");
            field2.setAccessible(true);
            Object value2 = field2.get(dataNode);
            Long acl = (Long) value2;

            return longKeyMap.get(acl.longValue());

        } catch (Exception ex) {

            //this should never happen
            return null;

        }

    }

}
