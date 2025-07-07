@echo off
REM Move into the script’s directory
pushd "%~dp0"

REM Ensure bin/ exists
if not exist bin mkdir bin

REM Compile all Java sources
javac -cp "lib/*" -d bin src\model\*.java src\util\*.java src\app\MainApp.java

REM Launch the app
java -cp "lib/*;bin" app.MainApp

popd
pause
