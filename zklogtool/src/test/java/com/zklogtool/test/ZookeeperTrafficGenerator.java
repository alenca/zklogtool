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

import static java.lang.Thread.sleep;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class ZookeeperTrafficGenerator {
  
    int interval;
    boolean running = false;   
    ZooKeeper zk;
    int nodesNumber;

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public void setNodesNumber(int nodesNumber) {
        this.nodesNumber = nodesNumber;
    }

    
    public void start(){
    
        if(!running){
        
            Runnable r = new Runnable(){

                @Override
                public void run() {
                    
                    int node=1;
                    
                    running = true;
                    
                    while(running){
                        
                        try {
                            
                            zk.create("/node"+node, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                                                   
                            node++;
                            
                            if(nodesNumber<node){
                                running = false;
                            }
                            
                            sleep(interval);
                            
                        } catch (Exception ex) {
                            
                        }
                    }
                    
                    
                }
            
            };
  
            Thread thread = new Thread(r, "ZookeeperTraffic");
            thread.start();
     
        }
    
    }
    
    public void stop(){
    
        running = false;
    
    }
    
}
