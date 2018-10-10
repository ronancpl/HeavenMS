@echo off
@title HeavenMS001
set CLASSPATH=.;dist\*
java -Xmx2048m -Dwzpath=wz\ -Xrunjdwp:transport=dt_socket,address=9000,server=y,suspend=n net.server.Server
pause
