@echo off
echo Adding Maven to System PATH...
echo.

REM Set Maven Home
set MAVEN_HOME=C:\Program Files\Apache\maven\apache-maven-3.9.12

REM Add to PATH permanently (requires admin)
setx /M PATH "%PATH%;%MAVEN_HOME%\bin"

echo.
echo ✅ Maven added to PATH!
echo.
echo Please close this terminal and open a NEW terminal for changes to take effect.
echo Then verify by running: mvn -version
echo.
pause
