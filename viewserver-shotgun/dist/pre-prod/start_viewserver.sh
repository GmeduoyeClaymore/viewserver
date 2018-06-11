#/bin/bash
nohup java -jar  -Xmx250m -Xms250m -XX:StringTableSize=1000003 -Dlog4j.configurationFile=log4j/log4j2-pre-prod.xml -Dshotgun.environment=pre-prod viewserver-shotgun-*.jar $1 &