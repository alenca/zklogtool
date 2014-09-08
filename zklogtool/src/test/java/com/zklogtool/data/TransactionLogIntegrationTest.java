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

import com.zklogtool.reader.TransactionLogReaderFactory;
import com.zklogtool.test.IntegrationTests;
import com.zklogtool.test.ZookeeperServer;
import com.zklogtool.test.ZookeeperTrafficGenerator;
import java.io.File;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

@Category({IntegrationTests.class})
public class TransactionLogIntegrationTest {

    Logger logger = getLogger(TransactionLogIntegrationTest.class);
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
 
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
        zookeeperTrafficGenerator.setNodesNumber(20);     
        zookeeperTrafficGenerator.setZk(zk);    
        zookeeperTrafficGenerator.start();
        
        
    }

    @After
    public void tearDown() throws Exception {

        zookeeperServer.stop();
        zookeeperTrafficGenerator.stop();
        
    }
    
    
    @Test
    public void test() throws NoFileException, InterruptedException, KeeperException{
        
        DataDirTransactionLogFileList s = new DataDirTransactionLogFileList(dataDir);

        TransactionLog transactionLog = new TransactionLog(s, new TransactionLogReaderFactory());

        logger.debug("DATA DIR PATH: "+dataDir.getAbsolutePath());
         
        TransactionIterator iterator = transactionLog.iterator();
        
        long expectedZxid = 1;
        
        int transactions = 0;
        
        TransactionState ts = null;
        
        TransactionState old = null;
        
        
        
        while(transactions<10){
            
            old = ts;
  
            ts = iterator.nextTransactionState();

            
            if(ts == TransactionState.OK){
                
                Transaction t = iterator.next();
                
                assertEquals(expectedZxid,t.getTxnHeader().getZxid());
                
                transactions++;
            
                expectedZxid++;
                
            }else if(ts == TransactionState.INCOMPLETE && ts!=old){
            
                logger.debug("TRANSACTION STATE INCOMPLETE");
                
            }else if(ts == TransactionState.CORRUPTION && ts!=old){
            
                logger.debug("TRANSACTION STATE CORRPUTION");
            
            }else if(ts == TransactionState.EMPTY && ts!=old){
            
                logger.debug("TRANSACTION STATE EMPTY");
            
            }
            
                   
        }
    
    }
    
    
}
