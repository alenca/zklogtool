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

import com.zklogtool.test.UnitTests;
import static com.zklogtool.util.Util.getZxidFromName;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({UnitTests.class})
public class UtilTest {

	@Test
	public void getZxidFromNameTest(){
		
		String name1 = "log.46";
		String name2 = "snapshot.44";
		
		long zxid1 = 70;
		long zxid2 = 68;
		
		assertEquals(zxid1,getZxidFromName(name1));
		assertEquals(zxid2,getZxidFromName(name2));
			
	}
	
}
