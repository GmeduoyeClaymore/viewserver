EMULATOR=$1

if [ -n "$EMULATOR" ]; then
    echo "Starting $EMULATOR"
	emulator -avd $EMULATOR &
	sleep 10
else
	echo "No AVD specified please specify AVD from list below"
	emulator -list-avds
fi

adb reverse tcp:7007 tcp:7007
react-native run-android
