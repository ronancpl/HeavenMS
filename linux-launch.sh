#!/bin/sh
# cores in classpath, thanks to lkxyyjx
export CLASSPATH=".:dist/*:cores/*"
java -Xmx2048m -Dwzpath=wz/ net.server.Server