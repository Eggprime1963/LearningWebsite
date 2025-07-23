# 🗄️ MySQL Database Setup Guide

## 📋 **Complete MySQL Configuration for Learning Management System**

This guide covers both local development and production deployment setup.

## 🔧 **Local Development Setup**

### 1. Install MySQL Server

#### Windows:
1. Download MySQL Installer from [mysql.com](https://dev.mysql.com/downloads/installer/)
2. Run installer and select "MySQL Server" + "MySQL Workbench"
3. Set root password: `DBProject@` (as configured in persistence.xml)
4. Start MySQL service

#### macOS:
```bash
# Using Homebrew
brew install mysql
brew services start mysql
mysql_secure_installation
```

#### Linux (Ubuntu/Debian):
```bash
sudo apt update
sudo apt install mysql-server
sudo mysql_secure_installation
```

### 2. Create Database and User

Connect to MySQL and run these commands:

```sql
-- Connect as root
mysql -u root -p

-- Create database
CREATE DATABASE learning_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user for the application
CREATE USER 'learning_user'@'localhost' IDENTIFIED BY 'learning_pass123';

-- Grant privileges
GRANT ALL PRIVILEGES ON learning_management.* TO 'learning_user'@'localhost';
FLUSH PRIVILEGES;

-- Use the database
USE learning_management;
```

### 3. Import Database Schema

```bash
# Navigate to your project directory
cd "C:\Users\Dang\Documents\New Folder\Project\LearningWebsite"

# Import the main schema
mysql -u learning_user -p learning_management < learning_management_main.sql
```

### 4. Verify Local Configuration

Your `persistence.xml` is configured for local development:
```xml
<property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/learning_management?useSSL=false&amp;serverTimezone=UTC"/>
<property name="jakarta.persistence.jdbc.user" value="root"/>
<property name="jakarta.persistence.jdbc.password" value="DBProject@"/>
```

## 🚀 **Production Setup (Vercel Deployment)**

### 1. Choose a MySQL Provider

#### Option A: PlanetScale (Recommended)
- Serverless MySQL platform
- Free tier available
- Built-in connection pooling
- Global edge database

**Setup:**
1. Go to [planetscale.com](https://planetscale.com)
2. Create account and new database
3. Get connection string format:
   ```
   mysql://username:password@host.planetscale.com:3306/database?sslaccept=strict
   ```

#### Option B: Railway
- Simple MySQL deployment
- Good for development/staging

**Setup:**
1. Go to [railway.app](https://railway.app)
2. Create MySQL service
3. Get connection string

#### Option C: AWS RDS
- Enterprise-grade MySQL
- More configuration required

#### Option D: Digital Ocean Managed Database
- Affordable managed MySQL
- Good performance

### 2. Set Environment Variables in Vercel

Go to your Vercel Dashboard → Project → Settings → Environment Variables:

```bash
# Required for all environments
DATABASE_URL=mysql://username:password@hostname:port/database_name

# Example with PlanetScale
DATABASE_URL=mysql://abc123:pscale_pw_xyz@aws.connect.psdb.cloud:3306/learning-platform?sslaccept=strict

# Example with Railway  
DATABASE_URL=mysql://root:password123@containers-us-west-123.railway.app:6789/railway

# Example with AWS RDS
DATABASE_URL=mysql://admin:password@learning-db.cluster-xyz.us-east-1.rds.amazonaws.com:3306/learning_management
```

### 3. Configure for Production

The `JPAUtil.java` class automatically detects production environment and parses `DATABASE_URL`:

- ✅ **Automatic SSL** for production connections
- ✅ **Connection pooling** with optimized settings
- ✅ **Environment detection** (local vs production)
- ✅ **Error handling** and logging

## 📊 **Database Schema**

Your database includes these main tables:

```sql
-- Users table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('student', 'teacher', 'admin') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Courses table  
CREATE TABLE courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    teacher_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- Lectures table
CREATE TABLE lectures (
    id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    video_url VARCHAR(500),
    lecture_order INT,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Assignments table
CREATE TABLE assignments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    due_date DATETIME,
    max_score INT DEFAULT 100,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Additional tables: enrollments, submissions, user_activities, etc.
```

## 🔍 **Testing Database Connection**

### Local Testing:
```bash
# Test MySQL connection
mysql -u root -p -e "SELECT VERSION();"

# Test application database
mysql -u root -p learning_management -e "SHOW TABLES;"
```

### Production Testing:
Your application includes diagnostic endpoints:
- Visit: `your-domain.com/diagnostic` 
- Check database connectivity status
- View connection information

## ⚙️ **Configuration Files Updated**

### 1. JPAUtil.java ✅
- Handles both local and production environments
- Automatic DATABASE_URL parsing
- Connection pooling for production
- Error handling and logging

### 2. pom.xml Dependencies ✅
```xml
<!-- MySQL Driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.3.0</version>
</dependency>

<!-- Hibernate JPA -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.4.4.Final</version>
</dependency>
```

### 3. persistence.xml ✅
- Configured for local development
- Production settings override via JPAUtil

## 🛠️ **Troubleshooting**

### Common Issues:

#### Connection Refused:
```bash
# Check MySQL service status
sudo systemctl status mysql    # Linux
brew services list | grep mysql # macOS
```

#### Authentication Failed:
```sql
-- Reset MySQL root password
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

#### Timezone Issues:
Add to MySQL configuration (`my.cnf`):
```ini
[mysqld]
default-time-zone='+00:00'
```

#### SSL Certificate Issues (Production):
Ensure your DATABASE_URL includes:
- `?sslaccept=strict` for PlanetScale
- `?useSSL=true` for other providers

### Debug Commands:
```bash
# Check database connection
mysql -u username -p -h hostname -P port database_name

# Test with Vercel CLI
vercel env ls
vercel logs
```

## ✅ **Verification Checklist**

### Local Development:
- [ ] MySQL server installed and running
- [ ] Database `learning_management` created
- [ ] Schema imported successfully
- [ ] Application connects locally
- [ ] JPA entities work correctly

### Production:
- [ ] Cloud MySQL database provisioned
- [ ] DATABASE_URL environment variable set in Vercel
- [ ] Application deploys successfully
- [ ] Database connectivity confirmed
- [ ] All features work in production

## 🚀 **Ready for Deployment**

Your MySQL configuration is now complete and production-ready:

- ✅ **Local Development**: Uses persistence.xml
- ✅ **Production**: Uses DATABASE_URL environment variable  
- ✅ **Connection Pooling**: Optimized for performance
- ✅ **SSL Support**: Secure production connections
- ✅ **Error Handling**: Comprehensive logging and diagnostics

---
**Database Setup Complete**: July 23, 2025  
**Status**: ✅ Ready for both local development and production deployment
