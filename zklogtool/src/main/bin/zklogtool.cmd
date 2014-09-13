@echo off
REM 
REM Copyright 2014 Alen Caljkusic.
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM

java -cp "%~dp0\lib\jcommander-1.35.jar;%~dp0\lib\log4j-1.2.14.jar;%~dp0\lib\slf4j-api-1.7.7.jar;%~dp0\lib\slf4j-log4j12-1.7.7.jar;%~dp0\lib\zookeeper-3.4.6.jar;%~dp0\${name}-${version}.jar" "com.zklogtool.cli.ZklogtoolMain"  %*
