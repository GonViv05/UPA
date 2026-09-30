#!/bin/sh
# Compila y ejecuta la versión de consola
cd "$(dirname "$0")"
javac -encoding UTF-8 -d out src/*.java && java -Dstdout.encoding=UTF-8 -cp out Main "$@"
