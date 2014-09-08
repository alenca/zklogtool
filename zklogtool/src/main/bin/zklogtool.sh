#!/bin/bash
# 
# Copyright 2014 Alen Čaljkušić.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

if [ "$JAVA_HOME" != "" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi

DIR="$(dirname $0)"

for i in $( cd $DIR/lib; LIBS=*; echo $LIBS;)
do
   CLASSPATH="$DIR/lib/$i:$CLASSPATH"
done

$JAVA -cp "$CLASSPATH$DIR/${name}-${version}.jar" "com.zklogtool.cli.ZklogtoolMain"  "$@"
