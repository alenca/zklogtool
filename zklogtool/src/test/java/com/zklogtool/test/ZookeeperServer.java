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
package com.zklogtool.test;

import java.io.File;
import java.io.IOException;
import static java.lang.String.valueOf;
import static java.lang.System.getProperties;
import static java.lang.Thread.interrupted;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;


public class ZookeeperServer {

    private ServerConfig serverConfig = new ServerConfig();

    private NIOServerCnxnFactory cnxnFactory;

    private ZooKeeperServer server;

    private String hostString;

    public void configureServer(File datadir, Integer tickTime, Integer maxConnections, int snapCount) throws Exception {
        
        ServerSocket s = new ServerSocket(0);
        Integer port = s.getLocalPort(); 
        s.close();
        
        Properties props = getProperties();
        props.setProperty("zookeeper.snapCount", valueOf(snapCount));
        
        hostString = "localhost:" + port;
        
        List<String> configArguments = new ArrayList<String>();
        if (port == null) {
            throw new IllegalArgumentException("The port must be specified");
        } else {
            configArguments.add(port.toString());
        }

        if (datadir == null) {
            throw new IllegalArgumentException("The datadir must be specified");
        } else {
            if (!datadir.exists()) {
                if (!datadir.mkdirs()) {
                    throw new RuntimeException("Unable to create datadir");
                }
            }
            configArguments.add(datadir.getAbsolutePath());
        }

        if (tickTime != null) {

            configArguments.add(tickTime.toString());
        }

        if (maxConnections != null) {

            if (configArguments.size() == 2) { // No tick time was specified, add a null to pad it
                configArguments.add(null);
            }
            configArguments.add(maxConnections.toString());
        }


        serverConfig.parse(configArguments.toArray(new String[configArguments.size()]));
       

        server = new ZooKeeperServer();

        FileTxnSnapLog transactionLog = new FileTxnSnapLog(
                new File(serverConfig.getDataDir()),
                new File(serverConfig.getDataDir())
        );
        
        
        
        server.setTxnLogFactory(transactionLog);
        server.setTickTime(serverConfig.getTickTime());
        server.setMinSessionTimeout(serverConfig.getMinSessionTimeout());
        server.setMaxSessionTimeout(serverConfig.getMaxSessionTimeout());


        cnxnFactory = new NIOServerCnxnFactory();
        cnxnFactory.setMaxClientCnxnsPerHost(serverConfig.getMaxClientCnxns());
        cnxnFactory.configure(serverConfig.getClientPortAddress(), 0);

    }

    public void start() throws Exception {
        //Anonymous Inner class to fork running the server process to a different thread.
        Runnable serverRunnable = new Runnable() {
            public void run() {
                try {
                    cnxnFactory.startup(server);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to start", e);
                } catch (InterruptedException e) {
                    interrupted();
                }
            }
        };

        Thread thread = new Thread(serverRunnable, "ZookeeperInProcess-Svr");
        thread.start();
    }


    public void stop() {

        if (server.isRunning()) {
            server.shutdown();
        }

        cnxnFactory.shutdown();
    }
    
    public ZooKeeper getZooKeeper() throws IOException {
        return new ZooKeeper(hostString, 2000, null);
    }
}
