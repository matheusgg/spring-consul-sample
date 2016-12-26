#!/bin/sh
BIND_INTERFACE=$(ip -o -4 addr list eth0 | head -n1 | awk '{print $4}' | cut -d/ -f1)
CONFIG_FILE="/tmp/consul-server-config.json"

/bin/cat <<EOM >$CONFIG_FILE
{
  "datacenter": "$DC",
  "data_dir": "/tmp/consul",
  "log_level": "$LOG_LEVEL",
  "node_name": "$NODE_NAME",
  "server": true,
  "bootstrap_expect": $BOOTSTRAP_EXPECT,
  "check_update_interval": "10s",
  "rejoin_after_leave": true,
  "ui": true,
  "bind_addr": "$BIND_INTERFACE",
  "client_addr": "$BIND_INTERFACE",
  "advertise_addr": "$ADVERTISE",
  "retry_join": [$JOIN],
  "ports": {
    "dns": $DNS_PORT,
    "http": $HTTP_PORT,
    "https": -1,
    "rpc": $RPC_PORT,
    "serf_lan": $SERF_LAN_PORT,
    "serf_wan": $SERF_WAN_PORT,
    "server": $SERVER_PORT
  }
}
EOM

CONSUL_COMAND="./consul agent -config-dir=/tmp & "

eval $CONSUL_COMAND

sleep 10

GIT_2_CONSUL_CONFIG="{
  \"version\": \"1.0\",
  \"local_store\": \"/tmp/git2consul_cache\",
  \"expand_keys_diff\": true,
  \"logger\" : {
    \"name\" : \"git2consul\",
    \"streams\" : [{
      \"level\": \"trace\",
      \"stream\": \"process.stdout\"
    },
    {
      \"level\": \"debug\",
      \"type\": \"rotating-file\",
      \"path\": \"/tmp/git2consul.log\"
    }]
  },
  \"repos\" : [{
    \"name\" : \"${GIT_INITIAL_REPO_NAME}\",
    \"url\" : \"${GIT_INITIAL_REPO}\",
    \"branches\" : [${GIT_INITIAL_REPO_BRANCHES}],
    \"hooks\": [{
      \"type\" : \"polling\",
      \"interval\" : \"1\"
    },
    {
      \"type\" : \"github\",
      \"port\" : $GIT_WEB_HOOK_PORT,
      \"url\" : \"/git2consul\"
    }]
  }]
}"

curl -X PUT -H "Content-Type: application/json" -d "$GIT_2_CONSUL_CONFIG" "http://$BIND_INTERFACE:$HTTP_PORT/v1/kv/git2consul/config"

eval "git2consul --endpoint $BIND_INTERFACE --port $HTTP_PORT"