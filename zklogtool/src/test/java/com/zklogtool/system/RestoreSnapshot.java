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
import com.zklogtool.test.ApplicationRunner;
import com.zklogtool.test.IntegrationTests;
import java.io.File;
import static java.lang.Thread.sleep;
import java.net.URL;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import static org.junit.contrib.java.lang.system.ExpectedSystemExit.none;
import org.junit.experimental.categories.Category;


@Category({IntegrationTests.class})
public class RestoreSnapshot {
 
    Logger logger = getLogger(RestoreSnapshot.class);
    
    @Rule
    public final ExpectedSystemExit exit = none();

    ApplicationRunner applicationRunner;
        
    File dataDir;
    File snap1;
    

    @Before
    public void setUp() throws Exception {
        
        applicationRunner = new ApplicationRunner();

        URL url = this.getClass().getResource("/dataDir1/version-2/");
        dataDir = new File(url.getFile());
        
        url = this.getClass().getResource("/dataDir1/version-2/snapshot.44");
        snap1 = new File(url.getFile());
        
    }

    @After
    public void tearDown() throws Exception {

        applicationRunner.cleanup();
        
    }
    
    
    @Test
    public void testLastZxid() throws InterruptedException{
        
        exit.expectSystemExitWithStatus(0);

        applicationRunner.startApp(new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.DATA_DIR, dataDir.getAbsolutePath(), Arguments.ZXID, "last"});
        
        sleep(5000);
        
        assertTrue(isLastZxid(applicationRunner.getStdOutputText(),253));

    }
    
    @Test
    public void test200Zxid() throws InterruptedException{
        
        exit.expectSystemExitWithStatus(0);

        applicationRunner.startApp(new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.DATA_DIR, dataDir.getAbsolutePath(), Arguments.ZXID, "200"});
        
        sleep(5000);
        
        assertTrue(isLastZxid(applicationRunner.getStdOutputText(),200));

    }
    
    @Test
    public void testFirstZxid() throws InterruptedException{
        
        exit.expectSystemExitWithStatus(0);

        applicationRunner.startApp(new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.DATA_DIR, dataDir.getAbsolutePath(), Arguments.ZXID, "first"});
        
        sleep(5000);
        
        assertTrue(isLastZxid(applicationRunner.getStdOutputText(),0));

    }
    
    private boolean isLastZxid(String output, long zxid){
        
        return output.contains("Last processed zxid: 0x"+Long.toString(zxid,16));
        
    }
    
}
