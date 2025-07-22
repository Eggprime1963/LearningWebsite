# 🎓 Learning Management System (LMS)

[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://openjdk.java.net/projects/jdk/11/)
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-10.0-blue.svg)](https://jakarta.ee/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-green.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A comprehensive web-based Learning Management System built with Jakarta EE, featuring role-based access control, course management, assignment tracking, and administrative tools.

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Usage](#-usage)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Screenshots](#-screenshots)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

### 🔐 Authentication & Authorization
- **Multi-Authentication**: Standard login + Google OAuth integration
- **Role-Based Access Control**: Student, Teacher, Admin roles with specific permissions
- **Secure Password Management**: BCrypt hashing for password security
- **Session Management**: Automatic session handling and timeout

### 👥 User Management
- **User Registration**: Self-registration with role selection
- **Profile Management**: Complete user profile with personal information
- **Admin User Control**: Full CRUD operations for user management
- **Activity Tracking**: User login/logout and activity monitoring

### 📚 Course Management
- **Course Creation**: Teachers can create and manage courses
- **Lecture Management**: Upload and organize lecture content
- **Assignment System**: Create, assign, and grade assignments
- **File Upload**: Support for course materials and student submissions

### 🎛️ Administrative Tools
- **Admin Dashboard**: Comprehensive user and system management
- **System Monitoring**: Real-time server health and performance metrics
- **Reporting System**: Detailed analytics and user reports
- **Export Functionality**: CSV export for data analysis

### 💻 User Experience
- **Responsive Design**: Mobile-friendly interface with Bootstrap 5
- **Intuitive Navigation**: Role-specific menus and workflows
- **Real-time Updates**: Dynamic content loading and status updates
- **Error Handling**: Comprehensive error pages and user feedback

## 🛠️ Technology Stack

### Backend
- **Java 11**: Core programming language
- **Jakarta EE 10**: Enterprise framework
- **Hibernate 6.4.4**: ORM for database operations
- **Maven**: Build automation and dependency management

### Frontend
- **JSP & JSTL**: Server-side rendering
- **Bootstrap 5.3**: Responsive CSS framework
- **Bootstrap Icons**: Icon library
- **JavaScript**: Client-side interactions

### Database
- **MySQL 8.0**: Primary database
- **JPA 3.0**: Java Persistence API
- **Connection Pooling**: Efficient database connections

### Security
- **BCrypt**: Password hashing
- **Google OAuth 2.0**: Third-party authentication
- **Custom Access Control**: Role-based security filters

### Development Tools
- **VS Code**: IDE with extensions
- **Git**: Version control
- **Maven**: Build automation

## 📁 Project Structure

```
LearningWebsite/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controller/          # Servlet controllers
│   │   │   │   ├── AdminServlet.java
│   │   │   │   ├── CourseServlet.java
│   │   │   │   ├── LoginServlet.java
│   │   │   │   └── ...
│   │   │   ├── dao/                 # Data Access Objects
│   │   │   │   ├── UserDAO.java
│   │   │   │   ├── CourseDAO.java
│   │   │   │   └── ...
│   │   │   ├── model/               # Entity classes
│   │   │   │   ├── User.java
│   │   │   │   ├── Course.java
│   │   │   │   └── ...
│   │   │   ├── util/                # Utility classes
│   │   │   │   └── AccessControl.java
│   │   │   └── filter/              # Security filters
│   │   │       └── AccessControlFilter.java
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       └── persistence.xml   # JPA configuration
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── jsp/             # JSP pages
│   │       │   │   ├── admin/       # Admin pages
│   │       │   │   └── ...
│   │       │   └── web.xml          # Web configuration
│   │       ├── css/                 # Stylesheets
│   │       ├── js/                  # JavaScript files
│   │       └── image/               # Static images
├── target/                          # Build output
├── pom.xml                         # Maven configuration
├── learning_management_main.sql    # Database schema
└── README.md                       # Project documentation
```

## 🚀 Installation

### Prerequisites
- **Java Development Kit (JDK) 11** or higher
- **Apache Maven 3.6+**
- **MySQL Server 8.0+**
- **Apache Tomcat 10** or **GlassFish 7** (Jakarta EE compatible)

### Step 1: Clone the Repository
```bash
git clone https://github.com/Eggprime1963/LearningWebsite.git
cd LearningWebsite
```

### Step 2: Database Setup
```sql
-- Create database
CREATE DATABASE learning_management;
USE learning_management;

-- Import schema
SOURCE learning_management_main.sql;

-- Verify installation
SHOW TABLES;
```

### Step 3: Configure Database Connection
Edit `src/main/resources/META-INF/persistence.xml`:
```xml
<property name="jakarta.persistence.jdbc.url" 
          value="jdbc:mysql://localhost:3306/learning_management?useSSL=false&amp;serverTimezone=UTC"/>
<property name="jakarta.persistence.jdbc.user" value="your_username"/>
<property name="jakarta.persistence.jdbc.password" value="your_password"/>
```

### Step 4: Build the Project
```bash
mvn clean package
```

### Step 5: Deploy to Application Server
```bash
# For Tomcat
cp target/learning-platform-1.0.0.war $TOMCAT_HOME/webapps/

# Start Tomcat
$TOMCAT_HOME/bin/startup.sh
```

### Step 6: Access the Application
Open your browser and navigate to:
```
http://localhost:8080/learning-platform-1.0.0/
```

## ⚙️ Configuration

### Database Configuration
The application uses MySQL with JPA/Hibernate. Key configuration files:

- **persistence.xml**: JPA configuration
- **web.xml**: Web application settings
- **pom.xml**: Maven dependencies

### Security Configuration
Role-based access control is implemented through:
- **AccessControl.java**: Permission utility class
- **AccessControlFilter.java**: Automatic route protection
- **User roles**: student, teacher, admin

### File Upload Configuration
Configure upload directories in:
- **CourseServlet.java**: Course image uploads
- **SubmissionServlet.java**: Assignment submissions

## 🎯 Usage

### For Students
1. **Register/Login**: Create account or login with Google
2. **Browse Courses**: View available courses and enroll
3. **Attend Lectures**: Access course content and materials
4. **Submit Assignments**: Upload assignment files
5. **Track Progress**: Monitor learning progress

### For Teachers
1. **Course Management**: Create and manage courses
2. **Content Upload**: Add lectures and materials
3. **Assignment Creation**: Create and manage assignments
4. **Student Management**: View enrolled students
5. **Grading**: Review and grade submissions

### For Administrators
1. **User Management**: Full CRUD operations on users
2. **System Monitoring**: Monitor system health and performance
3. **Report Generation**: Generate detailed analytics reports
4. **Role Management**: Assign and modify user roles
5. **System Maintenance**: Perform system maintenance tasks

## 📊 API Documentation

### Authentication Endpoints
```
POST /login              # Standard login
POST /auth/google        # Google OAuth login
POST /register           # User registration
GET  /logout             # User logout
```

### Course Management
```
GET  /courses            # List courses
POST /courses            # Create course
GET  /lectures           # List lectures
POST /lectures           # Create lecture
```

### Admin Endpoints
```
GET  /admin/dashboard    # Admin dashboard
GET  /admin/system       # System monitoring
GET  /admin/reports      # Analytics reports
POST /admin/users        # User management
```

## 🗄️ Database Schema

### Core Tables
- **users**: User account information
- **courses**: Course details and metadata
- **lectures**: Lecture content and materials
- **assignments**: Assignment information
- **submissions**: Student assignment submissions
- **enrollments**: Student-course relationships
- **user_activities**: User activity tracking

### Key Relationships
- User (1) → (N) Courses (teacher relationship)
- Course (1) → (N) Lectures
- Course (1) → (N) Assignments
- User (N) → (N) Courses (enrollment relationship)
- Assignment (1) → (N) Submissions

## 📸 Screenshots

### Student Dashboard
![Student Dashboard](docs/images/student-dashboard.png)

### Teacher Course Management
![Teacher Dashboard](docs/images/teacher-dashboard.png)

### Admin Panel
![Admin Panel](docs/images/admin-panel.png)

### System Monitoring
![System Monitor](docs/images/system-monitor.png)

## 🤝 Contributing

We welcome contributions to the Learning Management System! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit your changes** (`git commit -m 'Add amazing feature'`)
4. **Push to the branch** (`git push origin feature/amazing-feature`)
5. **Open a Pull Request**

### Development Guidelines
- Follow Java coding conventions
- Write comprehensive JavaDoc comments
- Include unit tests for new features
- Update documentation for API changes

### Code Style
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable and method names
- Include error handling and logging

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Jakarta EE Community** for the enterprise framework
- **Bootstrap Team** for the responsive CSS framework
- **Hibernate Team** for the excellent ORM solution
- **Google** for OAuth integration support

## 📞 Support

For support and questions:
- **Issues**: [GitHub Issues](https://github.com/Eggprime1963/LearningWebsite/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Eggprime1963/LearningWebsite/discussions)
- **Documentation**: [Project Wiki](https://github.com/Eggprime1963/LearningWebsite/wiki)

## 🔄 Version History

- **v1.0.0** (2025-07-21): Initial release with full feature set
  - Complete authentication system
  - Course and lecture management
  - Assignment submission system
  - Admin panel with monitoring
  - Responsive web interface

---

**Made with ❤️ for education and learning**
