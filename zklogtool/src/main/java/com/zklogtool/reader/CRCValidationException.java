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
package com.zklogtool.reader;

import java.io.IOException;

/**
 * Exception raised when CRC validation failed. Both snapshots and transaction
 * logs use Adler-32 checksum to ensure data integrity.
 *
 */
public class CRCValidationException extends IOException {

    public CRCValidationException(String message) {
        super(message);
    }

    public CRCValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
