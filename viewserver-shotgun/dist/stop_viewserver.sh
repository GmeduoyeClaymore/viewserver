#! /bin/bash
if cd /usr/bin/shotgun-viewserver &> /dev/null;
then
    pkill -f *viewserver-shotgun*
    echo "killed process"
fi

