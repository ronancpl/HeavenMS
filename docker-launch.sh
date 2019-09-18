#!/bin/bash

sed -i "0,/^\([[:space:]]*HOST: *\).*/s//\1xxx/;0,/\([[:space:]]*HOST: *\).*/s//\1${HOST}/;" config.yaml
sed -i "0,/^\([[:space:]]*DB_URL: *\).*/s//\1xxx/;0,/\([[:space:]]*DB_URL: *\).*/s//\1${DB_URL}/;" config.yaml
sed -i "0,/^\([[:space:]]*DB_USER: *\).*/s//\1xxx/;0,/\([[:space:]]*DB_USER: *\).*/s//\1${DB_USER}/;" config.yaml
sed -i "0,/^\([[:space:]]*DB_PASS: *\).*/s//\1xxx/;0,/\([[:space:]]*DB_PASS: *\).*/s//\1${DB_PASS}/;" config.yaml

exec sh ./posix-launch.sh