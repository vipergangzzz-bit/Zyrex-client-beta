@echo off
setlocal

set JAVA_HOME=C:\Users\kkk\graalvm\graalvm-ce-java8-20.3.0
set Path=%JAVA_HOME%\bin;%Path%

if not exist "%CD%\gradle\wrapper\gradle-wrapper.jar" (
    echo Downloading Gradle wrapper...
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v2.0.0/gradle/wrapper/gradle-wrapper.jar' -OutFile '%CD%\gradle\wrapper\gradle-wrapper.jar'"
    if not exist "%CD%\gradle\wrapper\gradle-wrapper.jar" (
        echo Failed to download gradle-wrapper.jar. Install Gradle manually or place the wrapper jar in gradle/wrapper/
        pause
        exit /b 1
    )
)

echo Setting up Forge workspace...
call gradlew setupDecompWorkspace

echo Building mod...
call gradlew build

echo.
echo Build complete! Output JAR is in build/libs/
