#!/usr/bin/env bash
set -e

# Launcher for macOS/Linux. Must be placed one level above App/.

# Change into the App folder
cd "$(dirname "$0")/App"

# Ensure bin/ exists inside App/
mkdir -p bin

# Compile Java sources into App/bin
javac -cp "lib/*" -d bin \
  src/model/*.java \
  src/util/*.java \
  src/app/MainApp.java

# Run the Swing application
java -cp "lib/*:bin" app.MainApp
