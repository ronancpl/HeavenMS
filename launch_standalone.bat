@echo off
@title MapleSolaxia Standalone
set CLASSPATH=.;dist\MapleSolaxia_STANDALONE.jar
java -Xmx2048m -Dwzpath=wz\ net.server.Server
pause