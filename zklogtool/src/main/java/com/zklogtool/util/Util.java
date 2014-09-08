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
package com.zklogtool.util;

import static java.lang.Long.parseLong;
import java.lang.reflect.Field;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.server.DataNode;

/**
 * Collection of static utility methods.
 */
public abstract class Util {

    /**
     * Returns zxid from snapshot of transaction log file name.
     *
     * @param name Name of snapshot or transaction log file.
     * @return Zxid extracted form file name.
     */
    public static long getZxidFromName(String name) {
        long zxid = -1;
        String nameParts[] = name.split("\\.");
        if (nameParts.length == 2 && (nameParts[0].equals("snapshot") || nameParts[0].equals("log"))) {
            try {
                zxid = parseLong(nameParts[1], 16);
            } catch (NumberFormatException e) {
            }
        }
        return zxid;
    }

    /**
     * Returns <code>String</code> representation of Zookeeper permission
     * integer.
     *
     * @param perms Permission integer. It is part of Zookeeper <code>ACL</code>
     * structure.
     * @return <code>String</code> representation of Zookeeper permission
     * integer.
     */
    public static String getACLString(int perms) {

        StringBuilder permsStringBuilder = new StringBuilder();

        if ((perms & ZooDefs.Perms.ALL) != 0) {
            permsStringBuilder.append("ALL");

        } else {

            if ((perms & ZooDefs.Perms.READ) != 0) {
                permsStringBuilder.append("READ ");

            }
            if ((perms & ZooDefs.Perms.WRITE) != 0) {
                permsStringBuilder.append("WRITE ");

            }
            if ((perms & ZooDefs.Perms.CREATE) != 0) {
                permsStringBuilder.append("CREATE ");

            }
            if ((perms & ZooDefs.Perms.DELETE) != 0) {
                permsStringBuilder.append("DELETE ");

            }
            if ((perms & ZooDefs.Perms.ADMIN) != 0) {
                permsStringBuilder.append("ADMIN ");

            }
        }

        return permsStringBuilder.toString().trim();

    }

    /**
     * Uses reflection to read data byte array from <code>DataNode</code>.
     *
     * @param dataNode <code>DataNode</code> to read data byte array from.
     * @return Data byte array associated with <code>dataNode</code>.
     */
    public static byte[] readData(DataNode dataNode) {

        try {

            Field field = DataNode.class.getDeclaredField("data");
            field.setAccessible(true);
            Object value = field.get(dataNode);
            return (byte[]) value;

        } catch (Exception ex) {

            //this should never happen
            return null;

        }

    }
    
    public static String longToHexString(long number){
    
        return "0x"+Long.toHexString(number);
        
    }

}
