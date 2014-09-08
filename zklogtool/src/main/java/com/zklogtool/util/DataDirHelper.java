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
package com.zklogtool.util;

import static com.zklogtool.util.Util.getZxidFromName;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import static java.util.Collections.sort;
import java.util.Comparator;
import java.util.List;

/**
 * Helper class used for finding and sorting transaction log and snapshot files.
 * 
 */
public class DataDirHelper {

    private final File transactionLogDir;
    private final File snapshotDir;
    
    FilenameFilter snapFilter = new FilenameFilter(){

            @Override
            public boolean accept(File file, String name) {
                if(name.startsWith("snapshot"))
                    return true;
                else
                    return false;
                
            }      
        
        };
    
    
    FilenameFilter logFilter = new FilenameFilter(){

            @Override
            public boolean accept(File file, String name) {
                if(name.startsWith("log"))
                    return true;
                else
                    return false;
                
            }      
        
        };
    
    Comparator<File> comparator = new Comparator<File>(){

        @Override
        public int compare(File t, File t1) {
            
            long first = getZxidFromName(t.getName());
            long second = getZxidFromName(t1.getName());
            
            if(first > second)
                return 1;
            else if(first == second)
                return 0;
            else
                return -1;     
            
        }        
    
    };

    /**
     *
     * @param transactionLogDir Directory where transaction log files are stored.
     * @param snapshotDir Directory where snapshot files are stored.
     */
    public DataDirHelper(File transactionLogDir, File snapshotDir) {
        this.transactionLogDir = transactionLogDir;
        this.snapshotDir = snapshotDir;
        
    }

    /**
     *
     * @return Lexicographically ordered list of snapshot files.
     */
    public List<File> getSortedSnapshotList() {
              
        List<File> files = new ArrayList<>();
        
        for(File f : snapshotDir.listFiles(snapFilter)){    
            files.add(f);
        }
        
        sort(files,comparator);
        
        return files;
    }

    /**
     *
     * @return Lexicographically ordered list of transaction log files.
     */
    public List<File> getSortedLogList() {  

        List<File> files = new ArrayList<>();

        for(File f : transactionLogDir.listFiles(logFilter)){    
            files.add(f);
        }
     
        sort(files,comparator);
        
        return files;
    }

}
