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
package com.zklogtool.system;

import com.zklogtool.cli.Arguments;
import com.zklogtool.test.ApplicationRunner;
import com.zklogtool.test.IntegrationTests;
import java.io.File;
import static java.lang.Thread.sleep;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import static org.junit.contrib.java.lang.system.ExpectedSystemExit.none;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
@Category({IntegrationTests.class})
public class SystemTest {

    static File emptyDir;
    static File validSnapshotFile;
    static File validTransactionFile;
    static File corruptedTransactionFile;
    static File validPropertiesFile;
    static File corruptedPropertiesFile;
    static File validDataDir;

    @Parameters(name = "System test {index}: {0}") 
    public static Iterable<Object[]> data() {
        
        URL url = SystemTest.class.getResource("/empty/");
        emptyDir = new File(url.getFile());

        url = SystemTest.class.getResource("/dataDir1/version-2/snapshot.44");
        validSnapshotFile = new File(url.getFile());

        url = SystemTest.class.getResource("/dataDir1/version-2/log.46");
        validTransactionFile = new File(url.getFile());

        url = SystemTest.class.getResource("/transactionLogs/corruptedLog.46.5");
        corruptedTransactionFile = new File(url.getFile());
        
        url = SystemTest.class.getResource("/conf/zoo.cfg.3");
        validPropertiesFile = new File(url.getFile());
        
        url = SystemTest.class.getResource("/conf/zoo.cfg.4");
        corruptedPropertiesFile = new File(url.getFile());
        
        url = SystemTest.class.getResource("/dataDir1/version-2/");
        validDataDir = new File(url.getFile());
        
        List<Object[]> params = new ArrayList<>();
          
        params.add(new Object[]{
            "Test version flag",
            new String[]{Arguments.VERSION},
            "Version",
            0
        });
         
        params.add(new Object[]{
            "Test help flag",
            new String[]{Arguments.HELP},
            "Usage",
            0
        });
            
        params.add(new Object[]{
            "Test no arguments",
            new String[]{""},
            "Usage",
            1
        });
           
        params.add(new Object[]{
            "Test wrong arguments 1",
            new String[]{"fsdfg", "fsdf", "gdfhs"},
            "Usage",
            1
        });

        params.add(new Object[]{
            "Test log command with wrong arguments",
            new String[]{Arguments.COMMAND_LOG, "fsdf", "gdfhs"},
            "Usage",
            1
        });

        params.add(new Object[]{
            "Test snapshot command with wrong arguments",
            new String[]{Arguments.COMMAND_SNAPSHOT, "fsdf", "gdfhs"},
            "Usage",
            1
        });

        params.add(new Object[]{
            "Test log command with no arguments",
            new String[]{Arguments.COMMAND_LOG},
            "One of following options must be used with " + Arguments.COMMAND_LOG + " command: " + Arguments.DATA_DIR + ", " + Arguments.LOG_FILE + " or " + Arguments.PROPERTIES_FILE,
            1
        });

        params.add(new Object[]{
            "Test snapshot command with no arguments",
            new String[]{Arguments.COMMAND_SNAPSHOT},
            "One of following options must be used with " + Arguments.COMMAND_SNAPSHOT + " command: " + Arguments.DATA_DIR + ", " + Arguments.SNAP_FILE + " or " + Arguments.PROPERTIES_FILE,
            1
        });

        params.add(new Object[]{
            "Test log command with not allowed combination of parameters",
            new String[]{Arguments.COMMAND_LOG, Arguments.DATA_LOG_DIR, "", Arguments.LOG_FILE, ""},
            Arguments.DATA_LOG_DIR + " option can not be used with " + Arguments.LOG_FILE + " option",
            1
        });

        params.add(new Object[]{
            "Test snapshot command with not allowed combination of parameters",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.PROPERTIES_FILE, "", Arguments.SNAP_FILE, ""},
            Arguments.SNAP_FILE + " option can not be used with " + Arguments.PROPERTIES_FILE + " option",
            1
        });
          
        params.add(new Object[]{
            "Test log command with unsupported options",
            new String[]{Arguments.COMMAND_LOG, Arguments.DATA_LOG_DIR, "", Arguments.ZXID, ""},
            "Usage",
            1
        });

        params.add(new Object[]{
            "Test snapshot command with unsupported options",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.PROPERTIES_FILE, "", Arguments.FOLLOW, ""},
            "Usage",
            1
        });

        params.add(new Object[]{
            "Test log command, " + Arguments.DATA_LOG_DIR + " with unexisting directory",
            new String[]{Arguments.COMMAND_LOG, Arguments.DATA_LOG_DIR, "aaa"},
            "Directory aaa not found",
            1
        });

        params.add(new Object[]{
            "Test log command, " + Arguments.DATA_LOG_DIR + " option with empty directory",
            new String[]{Arguments.COMMAND_LOG, Arguments.DATA_LOG_DIR, emptyDir.getAbsolutePath()},
            "",
            0
        });

        params.add(new Object[]{
            "Test log command, " + Arguments.LOG_FILE + " option with unexisting file",
            new String[]{Arguments.COMMAND_LOG, Arguments.LOG_FILE, "aaa"},
            "File aaa not found",
            1
        });

        params.add(new Object[]{
            "Test log command, " + Arguments.LOG_FILE + " option with directory",
            new String[]{Arguments.COMMAND_LOG, Arguments.LOG_FILE, emptyDir.getAbsolutePath()},
            emptyDir.getAbsolutePath() + " is directory",
            1
        });

        params.add(new Object[]{
            "Test log command, " + Arguments.LOG_FILE + " option with file that is not transaction log",
            new String[]{Arguments.COMMAND_LOG, Arguments.LOG_FILE, validSnapshotFile.getAbsolutePath()},
            "Data corruption",
            1
        });

        params.add(new Object[]{
            "Test log command, " + Arguments.LOG_FILE + " option with corrupted transaction log",
            new String[]{Arguments.COMMAND_LOG, Arguments.LOG_FILE, corruptedTransactionFile.getAbsolutePath()},
            "Next transaction partial",
            1
        });

        params.add(new Object[]{
            "Test log command, " + Arguments.LOG_FILE + " option with vaild transaction log",
            new String[]{Arguments.COMMAND_LOG, Arguments.LOG_FILE, validTransactionFile.getAbsolutePath()},
            "Zxid:\t\t0x85",
            0
        });

        params.add(new Object[]{
            "Test log command, " + Arguments.PROPERTIES_FILE + " option with unexisting file",
            new String[]{Arguments.COMMAND_LOG, Arguments.PROPERTIES_FILE, "aaa"},
            "File aaa not found",
            1
        });

        params.add(new Object[]{
            "Test log command, " + Arguments.PROPERTIES_FILE + " option with directory",
            new String[]{Arguments.COMMAND_LOG, Arguments.PROPERTIES_FILE, emptyDir.getAbsolutePath()},
            emptyDir.getAbsolutePath() + " is directory",
            1
        });

        params.add(new Object[]{
            "Test log command, " + Arguments.PROPERTIES_FILE + " option with file that is not properties file",
            new String[]{Arguments.COMMAND_LOG, Arguments.PROPERTIES_FILE, validSnapshotFile.getAbsolutePath()},
            "Problem in properties file",
            1
        });

        params.add(new Object[]{
            "Test log command, " + Arguments.PROPERTIES_FILE + " option with corrupted properties file",
            new String[]{Arguments.COMMAND_LOG, Arguments.PROPERTIES_FILE, corruptedPropertiesFile.getAbsolutePath()},
            "Problem in properties file",
            1
        });
        
        params.add(new Object[]{
            "Test log command, " + Arguments.PROPERTIES_FILE + " option with valid properties file",
            new String[]{Arguments.COMMAND_LOG, Arguments.PROPERTIES_FILE, validPropertiesFile.getAbsolutePath()},
            "Zxid:\t\t0x85",
            0
        });
        
        params.add(new Object[]{
            "Test snapshot command, " + Arguments.SNAP_FILE + " option with unexisting file",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.SNAP_FILE, "aaa"},
            "File aaa not found",
            1
        });

        params.add(new Object[]{
            "Test snapshot command, " + Arguments.SNAP_FILE + " option with directory",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.SNAP_FILE, emptyDir.getAbsolutePath()},
            emptyDir.getAbsolutePath() + " is directory",
            1
        });

        params.add(new Object[]{
            "Test snapshot command, " + Arguments.SNAP_FILE + " option with file that is not snapshot",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.SNAP_FILE, validTransactionFile.getAbsolutePath()},
            "Problem while reading file or corruption: ",
            1
        });

        params.add(new Object[]{
            "Test snapshot command, " + Arguments.SNAP_FILE + " option with corrupted snapshot",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.SNAP_FILE, corruptedTransactionFile.getAbsolutePath()},
            "Problem while reading file or corruption: ",
            1
        });

        params.add(new Object[]{
            "Test snapshot command, " + Arguments.SNAP_FILE + " option with vaild snapshot",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.SNAP_FILE, validSnapshotFile.getAbsolutePath()},
            "Path:\t\t/node66",
            0
        });
        
        params.add(new Object[]{
            "Test snapshot command, " + Arguments.SNAP_FILE + " option with vaild snapshot",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.SNAP_FILE, validSnapshotFile.getAbsolutePath()},
            "Path:\t\t/node66",
            0
        });
        
        params.add(new Object[]{
            "Test snapshot command, " + Arguments.DATA_DIR + " with unexisting directory",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.DATA_DIR, "aaa"},
            "Directory aaa not found",
            1
        });

        params.add(new Object[]{
            "Test snapshot command, " + Arguments.DATA_DIR + " option with empty directory",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.DATA_DIR, emptyDir.getAbsolutePath()},
            "No snapshot files found",
            1
        });
        
        params.add(new Object[]{
            "Test snapshot command, " + Arguments.DATA_DIR + " option with valid and " + Arguments.DATA_LOG_DIR + " empty",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.DATA_DIR, 
                validDataDir.getAbsolutePath(), Arguments.DATA_LOG_DIR, emptyDir.getAbsolutePath()},
            "Problem while reading transaction log",
            1
        });
        
        params.add(new Object[]{
            "Test snapshot command, " + Arguments.PROPERTIES_FILE + " option with file that is not properties file",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.PROPERTIES_FILE, validSnapshotFile.getAbsolutePath()},
            "Problem in properties file",
            1
        });

        params.add(new Object[]{
            "Test snapshot command, " + Arguments.PROPERTIES_FILE + " option with corrupted properties file",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.PROPERTIES_FILE, corruptedPropertiesFile.getAbsolutePath()},
            "Problem in properties file",
            1
        });
        
        params.add(new Object[]{
            "Test snapshot command, " + Arguments.PROPERTIES_FILE + " option with valid properties file",
            new String[]{Arguments.COMMAND_SNAPSHOT, Arguments.PROPERTIES_FILE, validPropertiesFile.getAbsolutePath()},
            "cZxid:\t\t0x58",
            0
        });
        
        
        
        return params;
    }
    

    @Rule
    public final ExpectedSystemExit exit = none();

    ApplicationRunner applicationRunner = new ApplicationRunner();

    //params
    String[] arguments;
    String output;
    int exitStatus;

    public SystemTest(String description, String[] arguments, String output, int exitStatus) {
        this.arguments = arguments;
        this.output = output;
        this.exitStatus = exitStatus;
   
    }

    @Before
    public void setUp() throws Exception {

        applicationRunner = new ApplicationRunner();

    }

    @After
    public void tearDown() throws Exception {

        applicationRunner.cleanup();

    }

    @Test
    public void test() throws InterruptedException {

        exit.expectSystemExitWithStatus(exitStatus);

        applicationRunner.startApp(arguments);

        //wait for exit
        sleep(1000);

        if (exitStatus == 0) {
            assertTrue(applicationRunner.getStdOutputText().contains(output));
        } else {
            assertTrue(applicationRunner.getErrOutputText().contains(output));
        }

    }


}
