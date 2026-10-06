@echo off
setlocal

echo.
echo ========================================
echo          WALKQUEST AI
echo ========================================
echo.
echo Searching for a free port...
echo.
set "PORT="

for %%P in (8080 8081 8082 8083 8084 8085 8086 8087 8088 8089) do (
    netstat -ano | findstr /R /C:":%%P .*LISTENING" >nul
    if errorlevel 1 (
        set "PORT=%%P"
        goto :port_found
    )
)

:port_found

if not defined PORT (
    echo ERROR: No free port found between 8080-8089.
    echo.
    pause
    exit /b 1
)

echo Free port found: %PORT%
echo.
echo Starting WalkQuest AI...
echo.
echo Open in browser:
echo http://localhost:%PORT%
echo.

call mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=%PORT%"

pause
