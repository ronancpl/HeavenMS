#!/bin/sh
export CLASSPATH=".:dist/*" 
java -Xmx3000m -Dwzpath=wz/ net.server.Server