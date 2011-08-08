@echo off
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

rem ---------------------------------------------------------------------------
rem Start script for the Channel Directory Solr
rem ---------------------------------------------------------------------------

rem Guess CHANNEL_DIRECTORY_HOME if not defined
set CURRENT_DIR=%cd%
if not "%CHANNEL_DIRECTORY_HOME%" == "" goto gotHome
set CHANNEL_DIRECTORY_HOME=%CURRENT_DIR%
if exist "%CHANNEL_DIRECTORY_HOME%\resources\solr\start.jar" goto okHome
cd ..
set CHANNEL_DIRECTORY_HOME=%cd%
cd %CURRENT_DIR%
:gotHome
if exist "%CHANNEL_DIRECTORY_HOME%\resources\solr\start.jar" goto okHome
echo The CHANNEL_DIRECTORY_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

rem Go to Solr home and start it
cd %CHANNEL_DIRECTORY_HOME%\resources\solr
java -Dsolr.solr.home=multicore -jar start.jar

:end