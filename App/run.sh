#!/usr/bin/env bash
set -e
# cd to the script’s directory
cd "$(dirname "$0")"

# Ensure bin/ exists
mkdir -p bin

# Compile all Java sources
javac -cp "lib/*" -d bin \
  src/model/*.java \
  src/util/*.java \
  src/app/MainApp.java

# Launch the app
java -cp "lib/*:bin" app.MainApp
