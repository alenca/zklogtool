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
package com.zklogtool.test;

import static com.zklogtool.cli.ZklogtoolMain.main;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static java.lang.System.setErr;
import static java.lang.System.setOut;

public class ApplicationRunner {

    private final ByteArrayOutputStream outputStream;
    private final ByteArrayOutputStream errorStream;
    private Thread thread;

    public ApplicationRunner(){

        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        
        setOut(new PrintStream(outputStream));
        setErr(new PrintStream(errorStream));
        
    }

    public void startApp(final String[] args) {
        thread = new Thread("Test Application") {
            @Override
            public void run() {
                    
                    main(args);
            }
        };
        thread.setDaemon(true);
        thread.start();
    }
    
    public void interrupt(){
    
        thread.interrupt();
    
    }
    
    public void cleanup(){
    
        setOut(System.out);
        setErr(System.err);
    
    }
    
    public String getStdOutputText(){
        
        return outputStream.toString();
                
    }
    
    public String getErrOutputText(){
        
        return errorStream.toString();
        
    }

}
