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
import java.io.IOException;
import java.net.URL;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({UnitTests.class})
public class PropertiesReaderTest {

    File prop1;
    File prop2;

    @Before
    public void setUp() {

        URL url = this.getClass().getResource("/conf/zoo.cfg.1");
        prop1 = new File(url.getFile());

        url = this.getClass().getResource("/conf/zoo.cfg.2");
        prop2 = new File(url.getFile());

    }

    @Test
    public void testSetup() {

        assertTrue(prop1.isFile());
        assertTrue(prop2.isFile());

    }

    @Test
    public void getTransactionLogDirTest() {

        try {

            PropertiesReader reader1 = new PropertiesReader(prop1);
            assertTrue(reader1.getTransactionLogDir().contentEquals("/home/zookeeper/datadir/version-2"));

            PropertiesReader reader2 = new PropertiesReader(prop2);
            assertTrue(reader2.getTransactionLogDir().contentEquals("/home/zookeeper/txnlogdir/version-2"));

        } catch (IOException e) {

            fail();
        }

    }

    @Test
    public void getSnapshotDirTest() {

        try {

            PropertiesReader reader1 = new PropertiesReader(prop1);
            assertTrue(reader1.getSnapshotDir().contentEquals("/home/zookeeper/datadir/version-2"));

            PropertiesReader reader2 = new PropertiesReader(prop2);
            assertTrue(reader2.getSnapshotDir().contentEquals("/home/zookeeper/datadir/version-2"));

        } catch (IOException e) {

            fail();
        }
    }

}
