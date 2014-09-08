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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Reads Zookeeper configuration file. Used to get directories where Zookeeper
 * stores transaction log and snapshot files.
 * 
 */
public class PropertiesReader {

    File propertiesFile;
    Properties prop;

    /**
     *
     * @param propertiesFile Zookeeper configuration file.
     * @throws IOException Thrown if there is a problem with reading
     * <code>propertiesFile</code>.
     */
    public PropertiesReader(File propertiesFile) throws IOException {

        this.propertiesFile = propertiesFile;

        prop = new Properties();
        prop.load(new FileInputStream(propertiesFile));

    }

    /**
     *
     * @return Directory where transaction log files are stored.
     */
    public String getTransactionLogDir() {

        if (prop.containsKey("dataLogDir")) {
            return prop.getProperty("dataLogDir") + "/version-2";
        } else if (prop.containsKey("dataDir")) {
            return prop.getProperty("dataDir") + "/version-2";
        } else {
            return null;
        }

    }

    /**
     *
     * @return Directory where snapshot files are stored.
     */
    public String getSnapshotDir() {

        if (prop.containsKey("dataDir")) {
            return prop.getProperty("dataDir") + "/version-2";
        } else {
            return null;
        }

    }

    public File getPropertiesFile() {
        return propertiesFile;
    }

}
