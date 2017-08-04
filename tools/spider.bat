@echo off
@title Drop Spider
set CLASSPATH=.;dist\*
java -server -Dwzpath=wz\ dropspider.Main
pause