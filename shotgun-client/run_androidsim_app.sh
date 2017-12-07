if [ -n "$EMULATOR" ]; then
   EMULATOR=$1
else
	DEVICES=( $(emulator -list-avds 2>&1 ) )
	EMULATOR=( ${DEVICES[0]} )
	echo "No AVD specified running emulator $EMULATOR"
fi

echo "Starting $EMULATOR"
emulator -avd $EMULATOR &
sleep 10

adb reverse tcp:7007 tcp:7007
adb reverse tcp:6060 tcp:6060
react-native run-android
