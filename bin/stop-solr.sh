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
# Stop script for the solr server
# -----------------------------------------------------------------------------

BIN_DIR=`dirname $0`
SOLR_PID=solr.pid
cd "$BIN_DIR"

if [ ! -f $SOLR_PID ]; then
  echo PID file does not exist
  exit 1
fi

kill `cat $SOLR_PID`
rm $SOLR_PID
exit 0
