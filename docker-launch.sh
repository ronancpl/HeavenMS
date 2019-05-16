#!/bin/bash

sed -i "s/HOST=.*/HOST=${HOST}/" configuration.ini
sed -i "s|URL=.*|URL=${URL}|" configuration.ini
sed -i "s/DB_USER=.*/DB_USER=${DB_USER}/" configuration.ini
sed -i "s/DB_PASS=.*/DB_PASS=${DB_PASS}/" configuration.ini

exec sh ./posix-launch.sh