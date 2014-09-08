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
package com.zklogtool.system;

import com.zklogtool.cli.Arguments;
import com.zklogtool.data.NoFileException;
import com.zklogtool.test.ApplicationRunner;
import com.zklogtool.test.IntegrationTests;
import com.zklogtool.test.ZookeeperServer;
import com.zklogtool.test.ZookeeperTrafficGenerator;
import java.io.File;
import static java.lang.Thread.sleep;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import static org.junit.contrib.java.lang.system.ExpectedSystemExit.none;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;


@Category({IntegrationTests.class})
public class LogCommandFollowOption {
   
    Logger logger = getLogger(LogCommandFollowOption.class);
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Rule
    public final ExpectedSystemExit exit = none();

    ApplicationRunner applicationRunner = new ApplicationRunner();
    ZookeeperServer zookeeperServer;
    ZookeeperTrafficGenerator zookeeperTrafficGenerator;
    
    File dataDir;
    ZooKeeper zk; 

    @Before
    public void setUp() throws Exception {

        File temp = tempFolder.newFolder("zk");
        
        dataDir = new File(temp.getAbsolutePath()+"/version-2"); 
        
        zookeeperServer = new ZookeeperServer();
        zookeeperServer.configureServer(temp, null, null,25);
        
        zookeeperServer.start();
        
        zk = zookeeperServer.getZooKeeper();
        
        zookeeperTrafficGenerator = new ZookeeperTrafficGenerator();       
        zookeeperTrafficGenerator.setInterval(1000);      
        zookeeperTrafficGenerator.setNodesNumber(10);     
        zookeeperTrafficGenerator.setZk(zk);    
        zookeeperTrafficGenerator.start();
        
        
    }

    @After
    public void tearDown() throws Exception {

        zookeeperServer.stop();
        zookeeperTrafficGenerator.stop();
       
    }
    
    
    @Test(timeout=20000)
    public void test() throws NoFileException, InterruptedException, KeeperException{
       
        exit.expectSystemExitWithStatus(1);
        
        applicationRunner.startApp(new String[]{Arguments.COMMAND_LOG, Arguments.DATA_LOG_DIR, dataDir.getAbsolutePath(), Arguments.FOLLOW});
        
        sleep(15000);
        
        applicationRunner.interrupt();
        
        assertTrue(applicationRunner.getStdOutputText().contains("Zxid:\t\t0xb"));
        assertFalse(applicationRunner.getStdOutputText().contains("Zxid:\t\t0xc"));       

    }
}
