@echo off
REM Requires JDK 21 and Gradle 9.2.1+ on PATH
gradle build
echo.
echo Jar is in build\libs\voidex-1.0.0.jar
pause
