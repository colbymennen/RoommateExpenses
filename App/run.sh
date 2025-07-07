#!/usr/bin/env bash
set -e

# 1) Make sure bin/ exists
mkdir -p bin

# 2) Compile all your code
javac -cp "lib/*" -d bin \
    src/model/*.java \
    src/util/*.java \
    src/app/MainApp.java

# 3) Launch the application
java -cp "lib/*;bin" app.MainApp
