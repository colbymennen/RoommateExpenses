#!/usr/bin/env bash
cd "$(dirname "$0")/App"
mkdir -p bin
javac -cp "lib/*" -d bin src/util/*.java src/model/*.java src/app/MainApp.java
java -cp "lib/*:bin" app.MainApp
