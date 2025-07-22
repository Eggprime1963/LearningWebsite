@echo off
REM Heroku Deployment Script for Learning Management System (Windows)
REM This script automates the deployment process to Heroku

echo 🚀 Starting Heroku deployment for Learning Management System...

REM Check if Heroku CLI is installed
where heroku >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ❌ Heroku CLI is not installed. Please install it first:
    echo    https://devcenter.heroku.com/articles/heroku-cli
    pause
    exit /b 1
)

REM Check if logged into Heroku
heroku auth:whoami >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo 🔐 Please log in to Heroku first:
    heroku auth:login
)

REM Get app name from user or use default
set /p APP_NAME="Enter your Heroku app name (or press Enter for default): "
if "%APP_NAME%"=="" (
    for /f %%i in ('powershell -command "Get-Date -UFormat %%s"') do set TIMESTAMP=%%i
    set APP_NAME=learning-platform-%TIMESTAMP%
    echo 📝 Using default app name: %APP_NAME%
)

echo 🏗️  Creating Heroku app: %APP_NAME%
heroku create %APP_NAME%

REM Add ClearDB MySQL addon
echo 🗄️  Adding ClearDB MySQL database...
heroku addons:create cleardb:ignite --app %APP_NAME%

REM Get database URL and configure
echo ⚙️  Configuring database...
for /f "tokens=*" %%i in ('heroku config:get CLEARDB_DATABASE_URL --app %APP_NAME%') do set DATABASE_URL=%%i
heroku config:set DATABASE_URL="%DATABASE_URL%" --app %APP_NAME%

REM Set environment variables
echo 🔧 Setting environment variables...
heroku config:set JAVA_OPTS="-Xmx512m -Xms256m" --app %APP_NAME%
heroku config:set MAVEN_CUSTOM_OPTS="-DskipTests=true" --app %APP_NAME%
heroku config:set APP_NAME="EduPlatform" --app %APP_NAME%

REM Build and deploy
echo 📦 Building and deploying application...
git add .
git commit -m "Deploy to Heroku - %date% %time%"

REM Add Heroku remote if it doesn't exist
git remote | find "heroku" >nul
if %ERRORLEVEL% neq 0 (
    git remote add heroku https://git.heroku.com/%APP_NAME%.git
)

REM Deploy to Heroku
git push heroku main

echo 🎉 Deployment completed!
echo 🌐 Your app is available at: https://%APP_NAME%.herokuapp.com
echo 🗄️  Database: ClearDB MySQL
echo 📊 Logs: heroku logs --tail --app %APP_NAME%

REM Open the app
set /p OPEN_APP="Would you like to open the app in your browser? (y/N): "
if /i "%OPEN_APP%"=="y" (
    heroku open --app %APP_NAME%
)

echo ✅ Deployment script completed successfully!
pause
