#!/bin/bash

sed -i "s/HOST=.*/HOST=${HOST}/" configuration.ini
sed -i "s|URL=.*|URL=${URL}|" configuration.ini
sed -i "s/DB_USER=.*/DB_USER=${DB_USER}/" configuration.ini
sed -i "s/DB_PASS=.*/DB_PASS=${DB_PASS}/" configuration.ini
sed -i "s/JAVA8=.*/JAVA8=TRUE/" configuration.ini

exec java -Xmx2048m -Dwzpath=wz -jar HeavenMS.jar