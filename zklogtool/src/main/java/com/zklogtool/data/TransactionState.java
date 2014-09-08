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
package com.zklogtool.data;

/**
 * Transactions are not atomically written to transaction log files. Therefor
 * transactions in transaction log files can be in a several states depending on
 * how much of data is written. <code>TransactionState</code> specifies several
 * different transaction states.
 *
 */
public enum TransactionState {

    /**
     * Transaction is completely written and CRC is successfully validated.
     */
    OK,
    /**
     * Transaction is not written at all.
     */
    EMPTY,
    /**
     * Part of transaction is written.
     */
    INCOMPLETE,
    /**
     * Part or entire transaction is written but CRC validation has failed.
     */
    CORRUPTION
}
