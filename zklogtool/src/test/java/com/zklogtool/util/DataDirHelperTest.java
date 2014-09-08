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
package com.zklogtool.util;

import com.zklogtool.test.UnitTests;
import java.io.File;
import java.net.URL;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;


@Category({UnitTests.class})
public class DataDirHelperTest {

    File dataDir;

    @Before
    public void setUp() {

        URL url = this.getClass().getResource("/dataDir1/version-2/");
        dataDir = new File(url.getFile());

    }

    @Test
    public void testSetup() {

        assertTrue(dataDir.isDirectory());

    }
    
    @Test
    public void getSortedSnapshotListTest(){
    
        DataDirHelper helper = new DataDirHelper(dataDir,dataDir);
        
        List<File> sortedSnapshotList = helper.getSortedSnapshotList();
        
        assertEquals(5,sortedSnapshotList.size());
        
        assertEquals(5,sortedSnapshotList.size());
        assertTrue(sortedSnapshotList.get(0).getName().contentEquals("snapshot.0"));
        assertTrue(sortedSnapshotList.get(1).getName().contentEquals("snapshot.44"));
        assertTrue(sortedSnapshotList.get(2).getName().contentEquals("snapshot.86"));
        assertTrue(sortedSnapshotList.get(3).getName().contentEquals("snapshot.b4"));
        assertTrue(sortedSnapshotList.get(4).getName().contentEquals("snapshot.e7"));
    
    }
    
    @Test
    public void getSortedLogListTest(){
    
        DataDirHelper helper = new DataDirHelper(dataDir,dataDir);
        
        List<File> sortedLogList = helper.getSortedLogList();
        
        assertEquals(5,sortedLogList.size());
        assertTrue(sortedLogList.get(0).getName().contentEquals("log.1"));
        assertTrue(sortedLogList.get(1).getName().contentEquals("log.46"));
        assertTrue(sortedLogList.get(2).getName().contentEquals("log.88"));
        assertTrue(sortedLogList.get(3).getName().contentEquals("log.b6"));
        assertTrue(sortedLogList.get(4).getName().contentEquals("log.e9"));
    
    }

}
