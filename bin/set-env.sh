#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# -----------------------------------------------------------------------------
# Set CHANNEL_DIRECTORY_HOME variable
# -----------------------------------------------------------------------------

checkHome() {
	if [ -f "$CHANNEL_DIRECTORY_HOME"/resources/start.jar ]; then
		CHECK_HOME=0
	fi
}

echoHomeErrorMessage() {
	echo The CHANNEL_DIRECTORY_HOME environment variable is not defined correctly
	echo This environment variable is needed to run this program
	exit 1
}

# Guess CHANNEL_DIRECTORY_HOME if not defined

if [ -z "$CHANNEL_DIRECTORY_HOME" ]; then
	
	CURRENT_DIR=`pwd`
	CHANNEL_DIRECTORY_HOME="$CURRENT_DIR"
	checkHome();
	
	if [ ! $CHECK_HOME -eq 0 ]; then
		
		CURRENT_DIR=`pwd ..`
		CHANNEL_DIRECTORY_HOME="$CURRENT_DIR"
		checkHome();
		
		if [ ! $CHECK_HOME -eq 0 ]; then
			echoHomeErrorMessage()
		fi
	fi
else
	checkHome()
	if [ ! $CHECK_HOME -eq 0 ]; then
		echoHomeErrorMessage()
	fi
fi

exit 0