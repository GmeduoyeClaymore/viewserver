#! /bin/bash

cd /usr/bin/shotgun-viewserver
source ./shotgun_env.sh

echo $SHOTGUN_ENVIRONMENT_NAME

if [[ -z "$SHOTGUN_ENVIRONMENT_NAME" ]]; then
    echo "Environment variable SHOTGUN_ENVIRONMENT_NAME is required and has not been set on this host"
   exit 1
fi
if [[ -z "$SHOTGUN_ENDPOINT" ]]; then
    echo "Environment variable SHOTGUN_ENDPOINT is required and has not been set on this host"
   exit 1
fi
if [[ -z "$SHOTGUN_ISMASTER" ]]; then
    echo "Environment variable SHOTGUN_ISMASTER is required and has not been set on this host"
   exit 1
fi
if [[ -z "$SHOTGUN_DOMAIN" ]]; then
    echo "Environment variable SHOTGUN_DOMAIN is required and has not been set on this host"
   exit 1
fi
nohup java -jar  -Xmx250m -Xms250m -XX:StringTableSize=1000003 -Dlog4j.configurationFile=log4j/log4j2-$SHOTGUN_ENVIRONMENT_NAME.xml -Dserver.isMaster=$SHOTGUN_DOMAIN -Dserver.isMaster=$SHOTGUN_ISMASTER -Dserver.endpoint=$SHOTGUN_ENDPOINT -Dshotgun.environment=$SHOTGUN_ENVIRONMENT_NAME viewserver-shotgun-*.jar > /dev/null 2> /dev/null < /dev/null &