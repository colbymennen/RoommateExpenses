@echo off
REM Launcher for Windows. Must be placed one level above App/.

REM Change directory into the App folder
pushd "%~dp0\App"

REM Ensure bin/ exists inside App/
if not exist bin mkdir bin

REM Compile Java sources into App/bin
javac -cp "lib/*" -d bin src\model\*.java src\util\*.java src\app\MainApp.java

REM Run the Swing application
java -cp "lib/*;bin" app.MainApp

REM Return to original directory
popd

pause
