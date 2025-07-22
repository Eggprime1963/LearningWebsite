#!/bin/bash

# Heroku Deployment Script for Learning Management System
# This script automates the deployment process to Heroku

echo "🚀 Starting Heroku deployment for Learning Management System..."

# Check if Heroku CLI is installed
if ! command -v heroku &> /dev/null; then
    echo "❌ Heroku CLI is not installed. Please install it first:"
    echo "   https://devcenter.heroku.com/articles/heroku-cli"
    exit 1
fi

# Check if logged into Heroku
if ! heroku auth:whoami &> /dev/null; then
    echo "🔐 Please log in to Heroku first:"
    heroku auth:login
fi

# Get app name from user or use default
read -p "Enter your Heroku app name (or press Enter for default): " APP_NAME
if [ -z "$APP_NAME" ]; then
    APP_NAME="learning-platform-$(date +%s)"
    echo "📝 Using default app name: $APP_NAME"
fi

echo "🏗️  Creating Heroku app: $APP_NAME"
heroku create $APP_NAME

# Add ClearDB MySQL addon
echo "🗄️  Adding ClearDB MySQL database..."
heroku addons:create cleardb:ignite --app $APP_NAME

# Get database URL and configure
echo "⚙️  Configuring database..."
DATABASE_URL=$(heroku config:get CLEARDB_DATABASE_URL --app $APP_NAME)
heroku config:set DATABASE_URL="$DATABASE_URL" --app $APP_NAME

# Set environment variables
echo "🔧 Setting environment variables..."
heroku config:set JAVA_OPTS="-Xmx512m -Xms256m" --app $APP_NAME
heroku config:set MAVEN_CUSTOM_OPTS="-DskipTests=true" --app $APP_NAME
heroku config:set APP_NAME="EduPlatform" --app $APP_NAME

# Build and deploy
echo "📦 Building and deploying application..."
git add .
git commit -m "Deploy to Heroku - $(date)"

# Add Heroku remote if it doesn't exist
if ! git remote | grep -q heroku; then
    git remote add heroku https://git.heroku.com/$APP_NAME.git
fi

# Deploy to Heroku
git push heroku main

echo "🎉 Deployment completed!"
echo "🌐 Your app is available at: https://$APP_NAME.herokuapp.com"
echo "🗄️  Database: ClearDB MySQL"
echo "📊 Logs: heroku logs --tail --app $APP_NAME"

# Open the app
read -p "Would you like to open the app in your browser? (y/N): " OPEN_APP
if [[ $OPEN_APP =~ ^[Yy]$ ]]; then
    heroku open --app $APP_NAME
fi

echo "✅ Deployment script completed successfully!"
