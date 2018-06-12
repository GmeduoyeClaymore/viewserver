#/bin/bash
cd /usr/bin/shotgun-viewserver
echo pwd
nohup java -jar  -Xmx250m -Xms250m -XX:StringTableSize=1000003 -Dlog4j.configurationFile=log4j/log4j2-pre-prod.xml -Dshotgun.environment=prod viewserver-shotgun-*.jar $1 &