@echo off
chcp 65001 >nul
title Library Management System - Production Environment

echo ========================================
echo  Library Management System - PRODUCTION
echo ========================================
echo.

set "BASE_DIR=%~dp0.."
set "BACKEND_DIR=%BASE_DIR%\backend"
set "FRONTEND_DIR=%BASE_DIR%\frontend"
set "LOG_DIR=%BASE_DIR%\logs"
set "UPLOADS_DIR=%BASE_DIR%\uploads"

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%UPLOADS_DIR%" mkdir "%UPLOADS_DIR%"

set "MYSQL_HOST=localhost"
set "MYSQL_PORT=3306"
set "MYSQL_DB=library_db"
set "MYSQL_USER=root"
set "MYSQL_PASSWORD=123456"

set "JWT_SECRET=changeThisToASecureRandomStringInProduction2024"
set "AES_SECRET_KEY=changeThisToA32ByteSecureKeyInProd"
set "CORS_ORIGINS=http://localhost,http://localhost:8080,http://localhost:80"

set "JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%LOG_DIR%"

set "SPRING_PROFILES_ACTIVE=prod"
set "ALERT_ENABLED=true"
set "ALERT_ERROR_THRESHOLD=10"
set "API_RATE_LIMIT=200"

echo [INFO] Starting Backend Service (Production Mode)...
echo [INFO] Profile: %SPRING_PROFILES_ACTIVE%
echo [INFO] MySQL: %MYSQL_HOST%:%MYSQL_PORT%/%MYSQL_DB%
echo [INFO] Logs: %LOG_DIR%
echo [INFO] Uploads: %UPLOADS_DIR%
echo [INFO] Alert System: Enabled (Threshold: %ALERT_ERROR_THRESHOLD%)
echo [INFO] API Rate Limit: %API_RATE_LIMIT%/minute
echo.

cd /d "%BACKEND_DIR%\target"

java %JAVA_OPTS% ^
    -jar library-backend.jar ^
    --spring.profiles.active=%SPRING_PROFILES_ACTIVE% ^
    --spring.datasource.url=jdbc:mysql://%MYSQL_HOST%:%MYSQL_PORT%/%MYSQL_DB%?characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true ^
    --spring.datasource.username=%MYSQL_USER% ^
    --spring.datasource.password=%MYSQL_PASSWORD% ^
    --jwt.secret=%JWT_SECRET% ^
    --aes.secret-key=%AES_SECRET_KEY% ^
    --cors.allowed-origins=%CORS_ORIGINS% ^
    --alert.enabled=%ALERT_ENABLED% ^
    --alert.error-threshold=%ALERT_ERROR_THRESHOLD% ^
    --api.rate-limit=%API_RATE_LIMIT% ^
    --logging.file.name=%LOG_DIR%\library-system-prod.log ^
    2>&1 | tee "%LOG_DIR%\console-output.log"