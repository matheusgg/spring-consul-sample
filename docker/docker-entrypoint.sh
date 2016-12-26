#!/bin/sh
sleep ${DELAY}
BIND_INTERFACE=$(ip -o -4 addr list eth0 | head -n1 | awk '{print $4}' | cut -d/ -f1)
eval "./consul agent -client=127.0.0.1 -node=${NODE_NAME} -advertise=${BIND_INTERFACE} -join=${JOIN} -datacenter=${DC} -data-dir=/tmp/consul &"
eval "java -jar /app.jar"