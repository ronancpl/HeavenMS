#!/bin/bash
# launch script
cores=$(echo cores/*)
cores=${cores// /:}
cp=.:dist:$cores

java -Xmx2048m -Dwzpath=wz -cp $cp net.server.Server
