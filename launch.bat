@echo off
@title HeavenMS
set PATH=C:\Program Files\Java\jdk1.8.0_211\bin;%PATH%
set CLASSPATH=.;dist\*
java -Xmx2048m -Dwzpath=wz\ net.server.Server
pause