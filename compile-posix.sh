#!/bin/bash
# compilation script for posix-compliant systems

src=src
dist=dist

cores=$(echo cores/*)
cores=${cores// /:}

mkdir -p $out
javac -d $dist -cp $cores $(find $src -name "*.java")
