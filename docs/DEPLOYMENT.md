# Learning Management System - Deployment Guide

## Production Deployment Checklist

### Pre-Deployment Requirements

#### System Requirements
- **Operating System**: Linux (Ubuntu 20.04+ or CentOS 8+) or Windows Server 2019+
- **Java Runtime**: OpenJDK 11 or Oracle JDK 11
- **Application Server**: Apache Tomcat 10.1+ or Eclipse GlassFish 7+
- **Database**: MySQL 8.0+ or MariaDB 10.6+
- **Memory**: Minimum 4GB RAM (8GB recommended)
- **Storage**: 20GB free space (50GB recommended)

#### Network Requirements
- Port 8080 (HTTP) or 8443 (HTTPS) for application server
- Port 3306 for MySQL database
- Outbound internet access for Google OAuth

### Step-by-Step Deployment

#### 1. Server Preparation

**Install Java 11:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-11-jdk

# CentOS/RHEL
sudo yum install java-11-openjdk-devel

# Verify installation
java -version
```

**Install Apache Tomcat 10:**
```bash
# Download and extract Tomcat
wget https://downloads.apache.org/tomcat/tomcat-10/v10.1.15/bin/apache-tomcat-10.1.15.tar.gz
tar -xzf apache-tomcat-10.1.15.tar.gz
sudo mv apache-tomcat-10.1.15 /opt/tomcat

# Create tomcat user
sudo useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat
sudo chown -R tomcat:tomcat /opt/tomcat

# Set environment variables
echo 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64' | sudo tee -a /etc/environment
echo 'export CATALINA_HOME=/opt/tomcat' | sudo tee -a /etc/environment
```

**Install MySQL:**
```bash
# Ubuntu/Debian
sudo apt install mysql-server mysql-client

# CentOS/RHEL
sudo yum install mysql-server mysql

# Start and enable MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# Secure installation
sudo mysql_secure_installation
```

#### 2. Database Setup

**Create Database and User:**
```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create database
CREATE DATABASE learning_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create application user
CREATE USER 'lms_user'@'localhost' IDENTIFIED BY 'secure_password_here';
GRANT ALL PRIVILEGES ON learning_management.* TO 'lms_user'@'localhost';
FLUSH PRIVILEGES;

-- Import schema
USE learning_management;
SOURCE /path/to/learning_management_main.sql;

-- Verify tables
SHOW TABLES;
```

**Database Optimization:**
```sql
-- Add indexes for performance
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_course_teacher ON courses(teacher_id);
CREATE INDEX idx_enrollment_user ON enrollments(user_id);
CREATE INDEX idx_enrollment_course ON enrollments(course_id);
```

#### 3. Application Configuration

**Update Database Configuration:**
Edit `src/main/resources/META-INF/persistence.xml`:
```xml
<property name="jakarta.persistence.jdbc.url" 
          value="jdbc:mysql://localhost:3306/learning_management?useSSL=true&amp;serverTimezone=UTC"/>
<property name="jakarta.persistence.jdbc.user" value="lms_user"/>
<property name="jakarta.persistence.jdbc.password" value="secure_password_here"/>
<property name="hibernate.hbm2ddl.auto" value="validate"/>
```

**Production Build:**
```bash
# Clone repository
git clone https://github.com/Eggprime1963/LearningWebsite.git
cd LearningWebsite

# Build application
mvn clean package -Pprod

# Verify WAR file
ls -la target/learning-platform-1.0.0.war
```

#### 4. SSL Configuration (Recommended)

**Generate SSL Certificate:**
```bash
# Self-signed certificate (for testing)
sudo keytool -genkey -alias tomcat -keyalg RSA -keystore /opt/tomcat/keystore.jks

# Or use Let's Encrypt for production
sudo certbot certonly --standalone -d yourdomain.com
```

**Configure Tomcat SSL:**
Edit `/opt/tomcat/conf/server.xml`:
```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true" scheme="https" secure="true"
           keystoreFile="/opt/tomcat/keystore.jks" keystorePass="your_keystore_password"
           clientAuth="false" sslProtocol="TLS" />
```

#### 5. Application Deployment

**Deploy WAR File:**
```bash
# Copy WAR to Tomcat webapps
sudo cp target/learning-platform-1.0.0.war /opt/tomcat/webapps/

# Set permissions
sudo chown tomcat:tomcat /opt/tomcat/webapps/learning-platform-1.0.0.war

# Start Tomcat
sudo systemctl start tomcat
sudo systemctl enable tomcat
```

**Create Upload Directories:**
```bash
# Create directories for file uploads
sudo mkdir -p /opt/tomcat/uploads/courses
sudo mkdir -p /opt/tomcat/uploads/assignments
sudo chown -R tomcat:tomcat /opt/tomcat/uploads
sudo chmod -R 755 /opt/tomcat/uploads
```

#### 6. Firewall Configuration

**Configure UFW (Ubuntu):**
```bash
sudo ufw allow 22/tcp      # SSH
sudo ufw allow 80/tcp      # HTTP
sudo ufw allow 443/tcp     # HTTPS
sudo ufw allow 8080/tcp    # Tomcat HTTP
sudo ufw allow 8443/tcp    # Tomcat HTTPS
sudo ufw enable
```

**Configure firewalld (CentOS):**
```bash
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=8443/tcp
sudo firewall-cmd --reload
```

### 7. System Service Configuration

**Create Tomcat Service:**
Create `/etc/systemd/system/tomcat.service`:
```ini
[Unit]
Description=Apache Tomcat Web Application Container
After=network.target

[Service]
Type=forking

Environment=JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid
Environment=CATALINA_HOME=/opt/tomcat
Environment=CATALINA_BASE=/opt/tomcat
Environment='CATALINA_OPTS=-Xms512M -Xmx2G -server -XX:+UseParallelGC'
Environment='JAVA_OPTS=-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom'

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

User=tomcat
Group=tomcat
UMask=0007
RestartSec=10
Restart=always

[Install]
WantedBy=multi-user.target
```

**Enable and Start Services:**
```bash
sudo systemctl daemon-reload
sudo systemctl enable tomcat
sudo systemctl start tomcat
sudo systemctl status tomcat
```

### 8. Monitoring and Logging

**Configure Log Rotation:**
Create `/etc/logrotate.d/tomcat`:
```
/opt/tomcat/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 644 tomcat tomcat
}
```

**Setup Monitoring:**
```bash
# Install monitoring tools
sudo apt install htop iotop nethogs

# Monitor Tomcat process
sudo systemctl status tomcat
sudo journalctl -u tomcat -f
```

### 9. Performance Tuning

**JVM Tuning:**
Update CATALINA_OPTS in tomcat.service:
```
Environment='CATALINA_OPTS=-Xms1G -Xmx4G -server -XX:+UseG1GC -XX:MaxGCPauseMillis=200'
```

**Database Tuning:**
Edit `/etc/mysql/mysql.conf.d/mysqld.cnf`:
```ini
[mysqld]
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
max_connections = 200
query_cache_size = 128M
```

### 10. Backup Strategy

**Database Backup Script:**
Create `/opt/scripts/backup_db.sh`:
```bash
#!/bin/bash
BACKUP_DIR="/opt/backups"
DB_NAME="learning_management"
DB_USER="lms_user"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR
mysqldump -u $DB_USER -p $DB_NAME > $BACKUP_DIR/lms_backup_$DATE.sql
gzip $BACKUP_DIR/lms_backup_$DATE.sql

# Keep only last 7 days of backups
find $BACKUP_DIR -name "lms_backup_*.sql.gz" -mtime +7 -delete
```

**Application Backup:**
```bash
#!/bin/bash
# Backup application files and uploads
tar -czf /opt/backups/lms_app_$(date +%Y%m%d).tar.gz \
    /opt/tomcat/webapps/learning-platform-1.0.0 \
    /opt/tomcat/uploads
```

### 11. Health Checks

**Application Health Check:**
```bash
#!/bin/bash
# Check if application is responding
curl -f http://localhost:8080/learning-platform-1.0.0/health || exit 1
```

**Database Health Check:**
```bash
#!/bin/bash
# Check database connectivity
mysql -u lms_user -p -e "SELECT 1" learning_management > /dev/null || exit 1
```

### 12. Security Hardening

**Remove Default Tomcat Apps:**
```bash
sudo rm -rf /opt/tomcat/webapps/ROOT
sudo rm -rf /opt/tomcat/webapps/docs
sudo rm -rf /opt/tomcat/webapps/examples
sudo rm -rf /opt/tomcat/webapps/host-manager
sudo rm -rf /opt/tomcat/webapps/manager
```

**Secure Tomcat Configuration:**
Edit `/opt/tomcat/conf/server.xml`:
```xml
<!-- Disable unnecessary ports -->
<!-- <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" /> -->

<!-- Add security headers -->
<Valve className="org.apache.catalina.valves.ErrorReportValve" 
       showReport="false" showServerInfo="false" />
```

### 13. Go Live Checklist

- [ ] Database created and populated
- [ ] Application deployed and accessible
- [ ] SSL certificate configured
- [ ] Firewall rules applied
- [ ] Monitoring setup complete
- [ ] Backup scripts configured
- [ ] Performance tuning applied
- [ ] Security hardening complete
- [ ] Health checks working
- [ ] Log rotation configured

### 14. Post-Deployment Tasks

**Create Admin User:**
```sql
-- Insert admin user (update password hash)
INSERT INTO users (username, email, password, role, first_name, last_name) 
VALUES ('admin', 'admin@yourdomain.com', '$2a$10$hashed_password_here', 'admin', 'System', 'Administrator');
```

**Verify Application:**
1. Access application at `https://yourdomain.com:8443/learning-platform-1.0.0/`
2. Test user registration and login
3. Test admin panel access
4. Verify file upload functionality
5. Check system monitoring dashboard

### Troubleshooting

**Common Issues:**
- **Port conflicts**: Check if ports 8080/8443 are available
- **Database connection**: Verify MySQL service and credentials
- **File permissions**: Ensure tomcat user has proper permissions
- **Memory issues**: Monitor JVM heap usage and adjust accordingly

**Log Files:**
- Application logs: `/opt/tomcat/logs/catalina.out`
- Access logs: `/opt/tomcat/logs/localhost_access_log.*.txt`
- MySQL logs: `/var/log/mysql/error.log`

---

*This deployment guide provides comprehensive instructions for production deployment of the Learning Management System.*
