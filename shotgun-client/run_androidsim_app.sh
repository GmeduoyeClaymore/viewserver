#!/bin/bash

while getopts e:p:r: option
do
 case "${option}"
 in
 e) EMULATOR=${OPTARG};;
 p) PORT=${OPTARG};;
 r) RUNREACT=${OPTARG};;
 esac
done

if [ -z "$EMULATOR" ] || [ -z "$PORT" ];
then
    echo "ERROR: you must specify avd name and port using -e and -p options. Exiting"
    exit 1
fi

IFS='+' read -r -a EMULATOR_ARR <<< "$EMULATOR"
IFS='+' read -r -a PORT_ARR <<< "$PORT"

for index in "${!EMULATOR_ARR[@]}"
do
    CURRENT_PORT=${PORT_ARR[index]}
    CURRENT_EMULATOR=${EMULATOR_ARR[index]}

    ADBNAME="emulator-"$CURRENT_PORT
    echo "Starting $CURRENT_EMULATOR on port $CURRENT_PORT"
    emulator -avd $CURRENT_EMULATOR -port $CURRENT_PORT &
    sleep 25

    adb -s $ADBNAME reverse tcp:7007 tcp:7007 &
    adb -s $ADBNAME reverse tcp:6060 tcp:6060 &
done

react-native run-android