# Heroku Deployment Automation Script
# This script helps deploy the Learning Management System to Heroku with proper database setup

param(
    [string]$AppName = "",
    [switch]$SkipBuild,
    [switch]$InitDB,
    [switch]$Help
)

if ($Help) {
    Write-Host @"
Heroku Deployment Script for Learning Management System

Usage: .\deploy-heroku.ps1 [OPTIONS]

Options:
  -AppName <name>    Specify Heroku app name (optional)
  -SkipBuild         Skip Maven build process
  -InitDB            Initialize database schema (default: true)
  -Help              Show this help message

Examples:
  .\deploy-heroku.ps1
  .\deploy-heroku.ps1 -AppName my-learning-app
  .\deploy-heroku.ps1 -SkipBuild -InitDB:$false

"@
    exit 0
}

Write-Host "🚀 Learning Management System - Heroku Deployment" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green

# Check prerequisites
Write-Host "`n📋 Checking prerequisites..." -ForegroundColor Yellow

# Check Heroku CLI
try {
    $herokuVersion = heroku --version 2>$null
    Write-Host "✅ Heroku CLI: $herokuVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Heroku CLI not found. Please install from: https://devcenter.heroku.com/articles/heroku-cli" -ForegroundColor Red
    exit 1
}

# Check Git
try {
    $gitVersion = git --version 2>$null
    Write-Host "✅ Git: $gitVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Git not found. Please install from: https://git-scm.com/downloads" -ForegroundColor Red
    exit 1
}

# Check if logged into Heroku
try {
    $herokuUser = heroku auth:whoami 2>$null
    Write-Host "✅ Logged in as: $herokuUser" -ForegroundColor Green
} catch {
    Write-Host "🔐 Please log in to Heroku:" -ForegroundColor Yellow
    heroku auth:login
}

# Initialize Git if needed
if (-not (Test-Path ".git")) {
    Write-Host "`n📁 Initializing Git repository..." -ForegroundColor Yellow
    git init
    git add .
    git commit -m "Initial commit with updated database schema"
}

# Build application if not skipped
if (-not $SkipBuild) {
    Write-Host "`n🔨 Building application..." -ForegroundColor Yellow
    
    # Check if Maven is available
    try {
        mvn --version 2>$null | Out-Null
        Write-Host "📦 Building with Maven..." -ForegroundColor Blue
        mvn clean package -DskipTests
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Maven build failed!" -ForegroundColor Red
            exit 1
        }
        Write-Host "✅ Build completed successfully" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  Maven not found. Using existing WAR file..." -ForegroundColor Yellow
        if (-not (Test-Path "target\learning-platform-1.0.0.war")) {
            Write-Host "❌ No WAR file found. Please build manually or install Maven." -ForegroundColor Red
            exit 1
        }
        Write-Host "✅ Found existing WAR file" -ForegroundColor Green
    }
}

# Get or generate app name
if ([string]::IsNullOrEmpty($AppName)) {
    $timestamp = [DateTimeOffset]::Now.ToUnixTimeSeconds()
    $AppName = "learning-platform-$timestamp"
    Write-Host "📝 Generated app name: $AppName" -ForegroundColor Blue
}

# Create Heroku app
Write-Host "`n🏗️  Creating Heroku application..." -ForegroundColor Yellow
try {
    heroku create $AppName
    Write-Host "✅ Created app: $AppName" -ForegroundColor Green
} catch {
    Write-Host "⚠️  App might already exist. Continuing..." -ForegroundColor Yellow
}

# Add ClearDB MySQL addon
Write-Host "`n🗄️  Setting up database..." -ForegroundColor Yellow
try {
    heroku addons:create cleardb:ignite --app $AppName
    Write-Host "✅ ClearDB MySQL addon added" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Database addon might already exist. Continuing..." -ForegroundColor Yellow
}

# Configure database
Write-Host "`n⚙️  Configuring database connection..." -ForegroundColor Yellow
try {
    $cleardbUrl = heroku config:get CLEARDB_DATABASE_URL --app $AppName
    heroku config:set "DATABASE_URL=$cleardbUrl" --app $AppName
    Write-Host "✅ Database URL configured" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to configure database URL" -ForegroundColor Red
}

# Set environment variables
Write-Host "`n🔧 Setting environment variables..." -ForegroundColor Yellow
heroku config:set JAVA_OPTS="-Xmx512m -Xms256m" --app $AppName
heroku config:set MAVEN_CUSTOM_OPTS="-DskipTests=true" --app $AppName
heroku config:set APP_NAME="EduPlatform" --app $AppName
Write-Host "✅ Environment variables set" -ForegroundColor Green

# Add Heroku remote if not exists
try {
    git remote get-url heroku 2>$null | Out-Null
} catch {
    git remote add heroku "https://git.heroku.com/$AppName.git"
    Write-Host "✅ Added Heroku git remote" -ForegroundColor Green
}

# Deploy application
Write-Host "`n🚀 Deploying application..." -ForegroundColor Yellow
git add .
git commit -m "Deploy to Heroku - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ErrorAction SilentlyContinue
git push heroku main

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Application deployed successfully!" -ForegroundColor Green
} else {
    Write-Host "❌ Deployment failed. Check logs with: heroku logs --tail --app $AppName" -ForegroundColor Red
    exit 1
}

# Display database initialization instructions
if (-not $InitDB) {
    Write-Host "`n💾 Database Initialization Required" -ForegroundColor Yellow
    Write-Host "=====================================" -ForegroundColor Yellow
    Write-Host "To complete setup, you need to initialize the database schema." -ForegroundColor White
    Write-Host ""
    Write-Host "Option 1 - Get database connection details:" -ForegroundColor Cyan
    Write-Host "heroku config:get CLEARDB_DATABASE_URL --app $AppName" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Option 2 - Use provided heroku-db-init.sql file:" -ForegroundColor Cyan
    Write-Host "1. Connect to your database using MySQL Workbench or command line" -ForegroundColor Gray
    Write-Host "2. Run the SQL script: heroku-db-init.sql" -ForegroundColor Gray
    Write-Host ""
    Write-Host "The database schema includes:" -ForegroundColor White
    Write-Host "- ✅ Users table with Google OAuth support" -ForegroundColor Green
    Write-Host "- ✅ Courses table with price field (decimal 10,2)" -ForegroundColor Green
    Write-Host "- ✅ Lectures, assignments, submissions tables" -ForegroundColor Green
    Write-Host "- ✅ Payments table for VNPay integration" -ForegroundColor Green
    Write-Host "- ✅ Sample data with ML course priced at 299,000 VND" -ForegroundColor Green
}

# Display final information
Write-Host "`n🎉 Deployment Summary" -ForegroundColor Green
Write-Host "===================" -ForegroundColor Green
Write-Host "App Name: $AppName" -ForegroundColor White
Write-Host "App URL: https://$AppName.herokuapp.com" -ForegroundColor Cyan
Write-Host ""
Write-Host "Useful commands:" -ForegroundColor Yellow
Write-Host "heroku logs --tail --app $AppName" -ForegroundColor Gray
Write-Host "heroku ps --app $AppName" -ForegroundColor Gray
Write-Host "heroku config --app $AppName" -ForegroundColor Gray
Write-Host "heroku open --app $AppName" -ForegroundColor Gray
Write-Host ""
Write-Host "📖 For detailed setup instructions, see: HEROKU_DEPLOYMENT_GUIDE.md" -ForegroundColor Blue

# Open application
$openApp = Read-Host "Would you like to open the application in your browser? (y/N)"
if ($openApp -eq "y" -or $openApp -eq "Y") {
    heroku open --app $AppName
}

Write-Host "`n✨ Deployment completed! Remember to initialize the database schema." -ForegroundColor Green
