# Heroku Deployment Guide for Learning Management System

## Prerequisites Setup

### 1. Install Required Tools

#### Install Heroku CLI
1. Download from: https://devcenter.heroku.com/articles/heroku-cli
2. Run the installer and restart your command prompt
3. Verify installation: `heroku --version`

#### Install Git (if not already installed)
1. Download from: https://git-scm.com/downloads
2. Install with default settings

#### Install Maven (if not already installed)
1. Download from: https://maven.apache.org/download.cgi
2. Add to PATH environment variable
3. Verify: `mvn --version`

### 2. Initial Setup

#### Login to Heroku
```bash
heroku login
```

#### Initialize Git Repository (if not done)
```bash
git init
git add .
git commit -m "Initial commit with updated database schema"
```

## Deployment Steps

### Step 1: Create Heroku Application
```bash
# Replace 'your-app-name' with your desired app name
heroku create your-learning-platform-app
```

### Step 2: Add ClearDB MySQL Add-on
```bash
heroku addons:create cleardb:ignite
```

### Step 3: Configure Database
```bash
# Get the database URL
heroku config:get CLEARDB_DATABASE_URL

# Set it as DATABASE_URL
heroku config:set DATABASE_URL=$(heroku config:get CLEARDB_DATABASE_URL)
```

### Step 4: Set Environment Variables
```bash
heroku config:set JAVA_OPTS="-Xmx512m -Xms256m"
heroku config:set MAVEN_CUSTOM_OPTS="-DskipTests=true"
heroku config:set APP_NAME="EduPlatform"
```

### Step 5: Initialize Database Schema
```bash
# Get database connection details
heroku config:get DATABASE_URL

# Connect to your database and run the heroku-db-init.sql script
# You can use MySQL Workbench, phpMyAdmin, or command line
```

#### Using MySQL Command Line:
1. Parse the DATABASE_URL to get connection details
2. Connect: `mysql -h hostname -u username -p database_name`
3. Run: `source heroku-db-init.sql`

### Step 6: Deploy Application
```bash
# Build the application
mvn clean package -DskipTests

# Commit any changes
git add .
git commit -m "Updated database schema with price field"

# Deploy to Heroku
git push heroku main
```

### Step 7: Verify Deployment
```bash
# Check application status
heroku ps

# View logs
heroku logs --tail

# Open application
heroku open
```

## Database Schema Alignment

### Key Updates Made:
1. ✅ Added `price` field to `courses` table (decimal(10,2) DEFAULT 0.00)
2. ✅ Updated course data with pricing (ML course: 299,000 VND, others: free)
3. ✅ Maintained all existing table structures and relationships
4. ✅ Updated Heroku initialization script to match local schema

### Tables Created:
- `users` - User management with Google OAuth support
- `courses` - Course catalog with pricing support
- `lectures` - Course content and videos
- `assignments` - Course assignments
- `submissions` - Student assignment submissions
- `enrollments` - Student course enrollments
- `payments` - VNPay payment integration
- `user_activity` - User activity tracking
- `coursemetadata` - Additional course metadata

## Troubleshooting

### Common Issues:

#### 1. Maven Not Found
```bash
# Install Maven or use the existing WAR file
# WAR file location: target/learning-platform-1.0.0.war
```

#### 2. Database Connection Issues
```bash
# Check database configuration
heroku config

# Reset database URL if needed
heroku config:set DATABASE_URL=$(heroku config:get CLEARDB_DATABASE_URL)
```

#### 3. Build Failures
```bash
# Check build logs
heroku logs --tail

# Verify Java version compatibility
heroku config:set JAVA_VERSION=11
```

#### 4. Database Schema Mismatch
- Ensure `heroku-db-init.sql` is run on the Heroku database
- Verify all tables are created with correct schema
- Check foreign key constraints are properly set

## Verification Steps

### 1. Database Verification
Connect to your Heroku database and verify:
```sql
-- Check if price field exists
DESCRIBE courses;

-- Verify course data with pricing
SELECT id, name, price FROM courses WHERE price > 0;

-- Check table count
SELECT COUNT(*) as table_count FROM information_schema.tables 
WHERE table_schema = 'your_database_name';
```

### 2. Application Verification
1. Access the application URL
2. Test user registration/login
3. Browse courses and verify pricing display
4. Test course filtering functionality
5. Verify premium course detection

## Next Steps

1. **SSL Configuration**: Set up SSL for production
2. **Domain Setup**: Configure custom domain if needed
3. **Monitoring**: Set up application monitoring
4. **Backup**: Configure database backups
5. **CI/CD**: Set up continuous deployment

## Support

If you encounter issues:
1. Check Heroku logs: `heroku logs --tail`
2. Verify database connection: `heroku pg:psql` (for PostgreSQL) or use MySQL client for ClearDB
3. Review application configuration: `heroku config`

## Important Notes

- The application uses ClearDB MySQL (not PostgreSQL)
- Price field is stored as decimal(10,2) for VND currency
- Premium courses are detected by price > 0
- All course filtering and pricing functionality is preserved
- VNPay integration is ready for payment processing

---

**Status**: ✅ Database schema updated and aligned
**Last Updated**: July 23, 2025
**Version**: 1.0.0
