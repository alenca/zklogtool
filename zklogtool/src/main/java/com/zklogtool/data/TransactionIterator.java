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

import java.util.Iterator;

/**
 * This is extension of <code>Iterator</code> interface with
 * <code>nextTransactionState()</code> method added. That method gives more
 * information about next <code>Transaction</code> than <code>hasNext()</code>
 * method.
 *
 */
public interface TransactionIterator extends Iterator<Transaction> {

    /**
     *
     * @return <code>TransactionState</code> of the next
     * <code>Transaction</code>.
     */
    public TransactionState nextTransactionState();

}
