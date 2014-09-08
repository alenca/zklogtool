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
package com.zklogtool.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.io.IOException;
import static java.lang.System.exit;
import java.util.Properties;

/**
 * Main class of command line tool. 
 * 
 */
public class ZklogtoolMain {

    public static void main(String[] args) {
        try {
            
            ZklogtoolMain zklogtool = new ZklogtoolMain(args);
            zklogtool.start();
            
        } catch (InterruptedException ex) {
            
            exit(1);
            
        }
    }

    @Parameter(names = Arguments.HELP, description = "Print help message", help = true)
    private boolean help;
    @Parameter(names = Arguments.VERSION, description = "Print zklogtool version")
    private boolean version;

    JCommander jc;
    CommandLog commandLog;
    CommandSnapshot commandSnapshot;

    String[] args;

    
    private ZklogtoolMain(String[] args) {

        this.args = args;

    }
    
    private String projectVersion() {

        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/app.properties"));
        } catch (IOException ex) {
            
            //if this happens something totaly went wrong because app.properties should alway be on classpath
            System.err.println("app.properties file is missing from classpath");
            exit(1);
            
        }
        return properties.getProperty("application.version");
    }

    private void start() throws InterruptedException{
        
        StringBuilder output = new StringBuilder();

        try {

            processArgs();

        } catch (Exception ex) {

            printHelp(output);

            System.err.println(output);

            exit(1);

        }

        if (version) {

            printVersion(output);

            System.out.println(output);

            exit(0);

        } else if (help) {

            printHelp(output);

            System.out.println(output);

            exit(0);
            
        } else if (jc.getParsedCommand()==null) {
                     
            printHelp(output);

            System.err.println(output);

            exit(1);
            
            
        } else if (jc.getParsedCommand().contentEquals(Arguments.COMMAND_LOG)) {
            
            commandLog.execute();
            
        } else if (jc.getParsedCommand().contentEquals(Arguments.COMMAND_SNAPSHOT)) {
            
            commandSnapshot.execute();
            
        }else{
        
            //not sure if this ever happens
            
            printHelp(output);

            System.err.println(output);

            exit(1);
        }
        
        exit(0);

    }

    
    // checks if combination of parameters is valid
    // exits with 1 if not
    private void processArgs() {

        jc = new JCommander(this);
        jc.setAcceptUnknownOptions(false);
        jc.setAllowAbbreviatedOptions(false);

        commandLog = new CommandLog();
        commandSnapshot = new CommandSnapshot();

        jc.addCommand(Arguments.COMMAND_LOG, commandLog);
        jc.addCommand(Arguments.COMMAND_SNAPSHOT, commandSnapshot);

        jc.setProgramName("zklogtool");

        jc.parseWithoutValidation(args);
        
        if(commandLog.startWithLastTransaction==true && commandLog.follow==false){
            System.err.println(Arguments.START_WITH_LAST_TRANSACTION + " flag can only be used with "+Arguments.FOLLOW+" flag");
            exit(1);
        }
        
        if(commandLog.dataLogDir!=null && commandLog.logFile!=null){
            System.err.println(Arguments.DATA_LOG_DIR + " option can not be used with "+Arguments.LOG_FILE+" option");
            exit(1);
        }
        
        if(commandLog.dataLogDir!=null && commandLog.propertiesFile!=null){
            System.err.println(Arguments.DATA_LOG_DIR + "option can not be used with "+Arguments.PROPERTIES_FILE + " option");
            exit(1);
        }
        
        if(commandLog.logFile!=null && commandLog.propertiesFile!=null){
            System.err.println(Arguments.LOG_FILE + " option can not be used with " + Arguments.PROPERTIES_FILE + " option");
            exit(1);
        }

        if((jc.getParsedCommand()!=null && jc.getParsedCommand().contentEquals("log")) 
                && commandLog.logFile==null && commandLog.propertiesFile==null && commandLog.dataLogDir==null){
            System.err.println("One of following options must be used with "+Arguments.COMMAND_LOG+" command: "+Arguments.DATA_DIR+", "+Arguments.LOG_FILE+" or "+Arguments.PROPERTIES_FILE);
            exit(1);
        }
        
        if(commandSnapshot.snapFile!=null && (commandSnapshot.dataDir!=null || commandSnapshot.dataLogDir!=null)){
            System.err.println(Arguments.SNAP_FILE+" option can not be used with "+Arguments.DATA_DIR+" or "+Arguments.DATA_LOG_DIR+" options");
            exit(1);
        }
        
        if(commandSnapshot.propertiesFile!=null && (commandSnapshot.dataDir!=null || commandSnapshot.dataLogDir!=null)){
            System.err.println(Arguments.PROPERTIES_FILE+" option can not be used "+Arguments.DATA_DIR+" or "+Arguments.DATA_LOG_DIR+" options");
            exit(1);
        }
        
        if(commandSnapshot.propertiesFile!=null && commandSnapshot.snapFile!=null){
            System.err.println(Arguments.SNAP_FILE+" option can not be used with "+Arguments.PROPERTIES_FILE+" option");
            exit(1);
        }
        
        if((jc.getParsedCommand()!=null && jc.getParsedCommand().contentEquals("snapshot")) 
                && commandSnapshot.dataDir==null && commandSnapshot.propertiesFile==null && commandSnapshot.snapFile==null){
            System.err.println("One of following options must be used with "+Arguments.COMMAND_SNAPSHOT+" command: "+Arguments.DATA_DIR+", "+Arguments.SNAP_FILE+" or "+Arguments.PROPERTIES_FILE);
            exit(1);
        }
        
        if(commandSnapshot.dataDir==null && commandSnapshot.dataLogDir!=null){
            System.err.println(Arguments.DATA_LOG_DIR+" option can not be used without "+Arguments.DATA_DIR+" option");
            exit(1);
        }
        
        
    }

    private void printHelp(StringBuilder sb) {

        jc.usage(sb);

    }

    private void printVersion(StringBuilder sb) {

        sb.append("Version: ").append(projectVersion());

    }


}
