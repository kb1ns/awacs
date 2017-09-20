#!/bin/sh
#   Copyright 2016-2017 AWACS Project.
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

AWACS_HOME=$(cd `dirname $0`/..; pwd)

if [ -d "$AWACS_HOME/plugins" ]; then
	PLUGIN_DIR="$AWACS_HOME/plugins"
fi

if [ -d "$AWACS_HOME/lib" ]; then
	LIB_DIR="$AWACS_HOME/lib"
fi

if [ -d "$AWACS_HOME/conf" ]; then
	CONF_DIR="$AWACS_HOME/conf"
fi

if [ -r "$AWACS_HOME/bin/setenv.sh" ]; then
	. "$AWACS_HOME/bin/setenv.sh"
fi

CLASS_PATH="$CONF_DIR"

for jar in $LIB_DIR/*.jar; do
	CLASS_PATH="$CLASS_PATH:$jar"
done

for jar in $PLUGIN_DIR/*.jar; do
	CLASS_PATH="$CLASS_PATH:$jar"
done

BOOTCLASS="io.awacs.server.Bootstrap"

STDOUT="$AWACS_HOME/awacs.log"
PID="$AWACS_HOME/awacs.pid"

JAVA_OPTS=`echo $JAVA_OPTS | tr -d "\r\t\n"`

echo "AWACS_HOME: $AWACS_HOME"
echo "Using JAVA_OPTS: $JAVA_OPTS"
echo "Using CLASSPATH: $CLASS_PATH"

nohup java -classpath "$CLASS_PATH" $JAVA_OPTS -Dawacs.home="$AWACS_HOME" "$BOOTCLASS">>"$STDOUT" 2>&1 & echo $!>"$PID"
