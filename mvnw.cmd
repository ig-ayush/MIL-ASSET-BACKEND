@echo off
@rem =========================================================================
@rem Maven Wrapper / Runner for Windows
@rem =========================================================================

setlocal

if defined JAVA_HOME goto checkJava
if exist "C:\Users\AYUSH ACHARYA\.gradle\jdks\jdk-21.0.4+7" (
    set "JAVA_HOME=C:\Users\AYUSH ACHARYA\.gradle\jdks\jdk-21.0.4+7"
    goto checkJava
)
if exist "C:\Users\AYUSH ACHARYA\.gradle\jdks\eclipse_adoptium-17-amd64-windows.2" (
    set "JAVA_HOME=C:\Users\AYUSH ACHARYA\.gradle\jdks\eclipse_adoptium-17-amd64-windows.2"
    goto checkJava
)

:checkJava
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo Error: JAVA_HOME is not set or valid. Please install Java 17+ or set JAVA_HOME.
    exit /b 1
)

set "MAVEN_CMD=C:\Users\AYUSH ACHARYA\.m2\apache-maven-3.9.6\bin\mvn.cmd"
if not exist "%MAVEN_CMD%" (
    echo Error: Maven not found at %MAVEN_CMD%
    exit /b 1
)

call "%MAVEN_CMD%" %*
exit /b %ERRORLEVEL%
