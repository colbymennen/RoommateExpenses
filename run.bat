@echo off
pushd %~dp0\App
if not exist bin mkdir bin
javac -cp "lib\*" -d bin src\util\*.java src\model\*.java src\app\MainApp.java
java -cp "lib\*;bin" app.MainApp
popd
