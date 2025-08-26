@echo off
echo Building RuneLite Ironman Ground Items Plugin...
echo.

REM Build the project and create the shadow JAR
call .\gradlew.bat shadowJar

REM Check if build was successful
if %ERRORLEVEL% neq 0 (
    echo.
    echo Build failed! Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo Build successful! Starting RuneLite with Ironman Ground Items Plugin...
echo.

REM Run RuneLite with the plugin (enable assertions and developer mode)
java -ea -jar build\libs\ironman-ground-items-1.0-SNAPSHOT-all.jar --developer-mode

REM Keep the window open if there's an error
if %ERRORLEVEL% neq 0 (
    echo.
    echo RuneLite exited with an error. Check the messages above.
    pause
)


