#!/bin/sh
export CLASSPATH=".:dist/*" 
java -Xmx2048m -Dwzpath=wz/ net.server.Server