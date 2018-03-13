#!/bin/bash
PROTO_FILES=`ls -m | sed -e 's/, / /g'`
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
$DIR/../../../../node_modules/protobufjs/bin/pbjs -t json $PROTO_FILES > $DIR/bundle.json
