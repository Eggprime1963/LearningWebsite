# Railway Deployment Guide for Learning Management System

## Overview
This guide will help you deploy your Java-based Learning Management System to Railway.app.

## Prerequisites
- Railway account (sign up at railway.app)
- GitHub repository with your code
- Your Gemini API key: `AIzaSyA8_WlPLW-vnYwFCJ7lXCOgbn5k1c0iePI`

## Step 1: Prepare Your Repository

Make sure your repository contains these files:
- `railway.toml` - Railway configuration
- `nixpacks.toml` - Build configuration  
- `Procfile` - Start command
- `pom.xml` - Maven build file (updated for JAR packaging)
- `src/main/java/util/Main.java` - Application entry point

## Step 2: Environment Variables

Set these environment variables in your Railway dashboard:

### Required Variables
```bash
# Database (Use Railway's MySQL add-on)
DATABASE_URL=mysql://username:password@host:port/database

# AI Service
GEMINI_API_KEY=AIzaSyA8_WlPLW-vnYwFCJ7lXCOgbn5k1c0iePI

# Server
PORT=8080
```

### Optional Variables (for payment features)
```bash
VNPAY_TMN_CODE=your_vnpay_code
VNPAY_HASH_SECRET=your_vnpay_secret  
VNPAY_RETURN_URL=https://your-app.railway.app/vnpay-result
```

## Step 3: Deploy to Railway

### Option A: GitHub Integration (Recommended)
1. Push your code to GitHub
2. Go to railway.app and sign in
3. Click "New Project"
4. Select "Deploy from GitHub repo"
5. Choose your repository
6. Railway will automatically detect it's a Java project

### Option B: Railway CLI
```bash
# Install Railway CLI
npm install -g @railway/cli

# Login to Railway
railway login

# Initialize project
railway init

# Deploy
railway up
```

## Step 4: Add Database

1. In your Railway dashboard, click "Add Service"
2. Select "MySQL" from the database options
3. Railway will automatically provide DATABASE_URL
4. Import your database schema using the provided credentials

## Step 5: Set Environment Variables

In your Railway project dashboard:
1. Go to "Variables" tab
2. Add the environment variables listed above
3. Deploy again to apply changes

## Step 6: Custom Domain (Optional)

1. Go to "Settings" > "Domains"
2. Add your custom domain
3. Update DNS records as instructed

## Troubleshooting

### Common Issues

**"Unable to access jarfile target/*jar"**
- This error occurs when the JAR file isn't built properly
- Solution: Our updated configuration builds a proper JAR file instead of WAR

**Build Failures**
- Check that all dependencies are available
- Verify Java 17 is being used
- Check the build logs in Railway dashboard

**Database Connection Issues**
- Verify DATABASE_URL is set correctly
- Check that the database service is running
- Ensure your IP is whitelisted (Railway handles this automatically)

### Build Process

The application now:
1. Builds as a JAR file (not WAR)
2. Includes embedded Tomcat server
3. Uses environment variables for configuration
4. Supports Railway's deployment system

### Key Changes Made

1. **POM.xml Updates**:
   - Changed packaging from WAR to JAR
   - Added embedded Tomcat dependencies
   - Added Maven Shade plugin for executable JAR

2. **Main Class**:
   - Created `util.Main` class to bootstrap Tomcat
   - Configures environment variables
   - Sets up web application context

3. **Configuration Files**:
   - `railway.toml`: Railway-specific settings
   - `nixpacks.toml`: Build configuration
   - `Procfile`: Start command

4. **AI Service**:
   - Switched from Ollama to Gemini API as primary
   - Hardcoded your API key as fallback
   - Environment variable support

## Testing Locally

To test the JAR locally (if you have Maven installed):
```bash
mvn clean package -DskipTests
java -Dserver.port=8080 -jar target/learning-platform-1.0.0.jar
```

## Support

If you encounter issues:
1. Check Railway deployment logs
2. Verify all environment variables are set
3. Ensure your database is accessible
4. Check that the Gemini API key is working

Your application should now deploy successfully to Railway!
