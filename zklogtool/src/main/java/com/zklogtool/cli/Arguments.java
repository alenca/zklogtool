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

/**
 * Command line tool arguments.
 *
 */
public abstract class Arguments {

    //flags
    public final static String VERSION = "--version";

    public final static String HELP = "--help";

    //commands
    public final static String COMMAND_LOG = "log";

    public final static String COMMAND_SNAPSHOT = "snapshot";

    //shared command options
    public final static String DATA_LOG_DIR = "-data-log-dir";

    public final static String PROPERTIES_FILE = "-properties-file";

    public final static String DATA_DECODER = "-data-decoder";

    //log options  
    public final static String LOG_FILE = "-log-file";

    //log flags
    public final static String FOLLOW = "--follow";

    public final static String START_WITH_LAST_TRANSACTION = "--start-with-last-transaction";

    //snapshot options
    public final static String ZXID = "-zxid";

    public final static String DATA_DIR = "-data-dir";

    public final static String SNAP_FILE = "-snap-file";

}
