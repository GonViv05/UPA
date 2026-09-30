@echo off
REM Compila y ejecuta la version de consola
cd /d "%~dp0"
chcp 65001 > nul
javac -encoding UTF-8 -d out src\*.java && java -Dstdout.encoding=UTF-8 -Dfile.encoding=UTF-8 -cp out Main %*
