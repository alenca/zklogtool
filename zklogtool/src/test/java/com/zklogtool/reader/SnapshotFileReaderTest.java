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

import com.zklogtool.data.DataState;
import com.zklogtool.printer.DataNodePrinter;
import com.zklogtool.printer.UnicodeDecoder;
import com.zklogtool.test.UnitTests;
import java.io.File;
import java.io.IOException;
import static java.lang.System.lineSeparator;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import org.apache.zookeeper.server.DataNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({UnitTests.class})
public class SnapshotFileReaderTest {

    Logger logger = getLogger(SnapshotFileReaderTest.class);
    
    File snap1;
    File snap2;

    long TS1 = 68;
    long TS2 = 68;

    @Before
    public void setUp() {

        URL url = this.getClass().getResource("/dataDir1/version-2/snapshot.44");
        snap1 = new File(url.getFile());

        url = this.getClass().getResource("/snapshots/corruptedSnapshot");
        snap2 = new File(url.getFile());
    }

    @Test
    public void testSetup() {

        assertTrue(snap1.isFile());
        assertTrue(snap2.isFile());

    }

    @Test
    public void loadSnapshotTest() {

        try {

            SnapshotFileReader r = new SnapshotFileReader(snap1, TS1);
            DataState snapshot;

            DataState fuzzyDataState = r.readFuzzySnapshot();

            assertEquals(69,fuzzyDataState.getLastZxid());  

            Map<Long, Integer> sessions = fuzzyDataState.getSessions();

            assertEquals(2,sessions.size());
            assertEquals(71,fuzzyDataState.getNodeCount());

            logger.debug("Node count: " + fuzzyDataState.getNodeCount());

            logger.debug("Sessions count: " + sessions.size());

            Iterator it = sessions.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                logger.debug("Session name: " + pairs.getKey() + " Session timeout " + pairs.getValue());
            }

            
            StringBuilder sb = new StringBuilder();
            DataNodePrinter printer = new DataNodePrinter(sb, new UnicodeDecoder());
            
            Iterator<Map.Entry<String, DataNode>> it2 = fuzzyDataState.getNodes().entrySet().iterator();
            while (it2.hasNext()) {

                
                
                Entry<String,DataNode> entry = it2.next();
                /*
                long mzxid = entry.getValue().stat.getMzxid();
                long pzxid = entry.getValue().stat.getPzxid();
                long czxid = entry.getValue().stat.getCzxid();
                String path = entry.getKey();
                */
                sb.append("Path:\t\t").append(entry.getKey()).append(lineSeparator());
                printer.printDataNode(entry.getValue(),fuzzyDataState);
                sb.append(lineSeparator()).append(lineSeparator());
                
                //logger.debug("Path: "+path+" mzxid= "+mzxid+" pzxid= "+pzxid+" czxid= "+czxid);
                
            }
            
            logger.debug(sb);
            
        } catch (IOException e) {

            fail();
        }

    }

    @Test
    public void loadCorruptedSnapshotTest() {

        try {

            SnapshotFileReader r = new SnapshotFileReader(snap2, TS2);
            
            r.readFuzzySnapshot();

            fail();

        } catch (IOException e) {

            assertTrue(e.getMessage().contains("CRC"));
        }

    }

}
