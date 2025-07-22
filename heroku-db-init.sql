-- Heroku Database Initialization Script
-- This script creates the necessary tables and inserts sample data
-- Designed to work with ClearDB MySQL on Heroku

-- Create Users table
CREATE TABLE IF NOT EXISTS Users (
    idUser INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role ENUM('student', 'teacher', 'admin') NOT NULL,
    fullName VARCHAR(100),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Courses table  
CREATE TABLE IF NOT EXISTS Courses (
    idCourse INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    teacherName VARCHAR(100),
    image VARCHAR(255),
    lectureCount INT DEFAULT 0,
    difficulty ENUM('beginner', 'intermediate', 'advanced') DEFAULT 'beginner',
    category VARCHAR(50),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Lectures table
CREATE TABLE IF NOT EXISTS Lectures (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    videoUrl VARCHAR(500),
    courseId INT,
    lectureOrder INT,
    duration INT,
    isPublished BOOLEAN DEFAULT TRUE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (courseId) REFERENCES Courses(idCourse) ON DELETE CASCADE
);

-- Create Enrollments table
CREATE TABLE IF NOT EXISTS Enrollments (
    studentId INT,
    courseId INT,
    enrollmentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    progress DECIMAL(5,2) DEFAULT 0.00,
    lastAccessDate TIMESTAMP,
    PRIMARY KEY (studentId, courseId),
    FOREIGN KEY (studentId) REFERENCES Users(idUser) ON DELETE CASCADE,
    FOREIGN KEY (courseId) REFERENCES Courses(idCourse) ON DELETE CASCADE
);

-- Create Assignments table
CREATE TABLE IF NOT EXISTS Assignments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    courseId INT,
    dueDate DATETIME,
    maxPoints INT DEFAULT 100,
    isPublished BOOLEAN DEFAULT TRUE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (courseId) REFERENCES Courses(idCourse) ON DELETE CASCADE
);

-- Create Submissions table
CREATE TABLE IF NOT EXISTS Submissions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    assignmentId INT,
    studentId INT,
    content TEXT,
    attachmentPath VARCHAR(255),
    submissionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    grade INT,
    feedback TEXT,
    isGraded BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (assignmentId) REFERENCES Assignments(id) ON DELETE CASCADE,
    FOREIGN KEY (studentId) REFERENCES Users(idUser) ON DELETE CASCADE
);

-- Create User Activity table
CREATE TABLE IF NOT EXISTS UserActivity (
    id INT PRIMARY KEY AUTO_INCREMENT,
    userId INT,
    activityType VARCHAR(50),
    description TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES Users(idUser) ON DELETE CASCADE
);

-- Create Chat History table for AI interactions
CREATE TABLE IF NOT EXISTS ChatHistory (
    id INT PRIMARY KEY AUTO_INCREMENT,
    userId INT,
    message TEXT,
    response TEXT,
    chatType VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES Users(idUser) ON DELETE CASCADE
);

-- Insert sample admin user (password: admin123)
INSERT IGNORE INTO Users (username, password, email, role, fullName) VALUES
('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRdvabm6dOwgaLyFSnfX6sMzqDy', 'admin@eduplatform.com', 'admin', 'System Administrator');

-- Insert sample teacher (password: teacher123)
INSERT IGNORE INTO Users (username, password, email, role, fullName) VALUES
('teacher1', '$2a$10$N.2E1msQ.UWY4uuJL24L/.YUH4Gd6pPTZ8WQD.ZEORrkkS4zG2k6O', 'teacher@eduplatform.com', 'teacher', 'John Smith');

-- Insert sample student (password: student123)
INSERT IGNORE INTO Users (username, password, email, role, fullName) VALUES
('student1', '$2a$10$JQcuFGZIkdK.UW.Oe6.6OO.rNk5yQKdGKFo6z7vTTr6zYmE9C1G4C', 'student@eduplatform.com', 'student', 'Jane Doe');

-- Insert sample courses
INSERT IGNORE INTO Courses (name, description, teacherName, image, lectureCount, difficulty, category) VALUES
('Introduction to Java Programming', 'Learn the fundamentals of Java programming from scratch', 'John Smith', 'javaf.png', 12, 'beginner', 'Programming'),
('Web Development with JavaScript', 'Master modern JavaScript and web development techniques', 'John Smith', 'js.png', 15, 'intermediate', 'Web Development'),
('Python for Data Science', 'Explore data science concepts using Python programming', 'John Smith', 'python.png', 18, 'intermediate', 'Data Science'),
('Advanced Database Design', 'Learn advanced database concepts and optimization techniques', 'John Smith', 'c.png', 10, 'advanced', 'Database'),
('Mobile App Development', 'Build mobile applications for iOS and Android platforms', 'John Smith', 'swift.png', 20, 'intermediate', 'Mobile Development');

-- Insert sample lectures for Java course
INSERT IGNORE INTO Lectures (title, content, videoUrl, courseId, lectureOrder, duration) VALUES
('Introduction to Java', 'Welcome to Java programming. In this lecture, we will cover the basics of Java.', 'https://example.com/java-intro', 1, 1, 45),
('Variables and Data Types', 'Learn about different data types and how to declare variables in Java.', 'https://example.com/java-variables', 1, 2, 50),
('Control Structures', 'Understanding if-else statements, loops, and switch cases in Java.', 'https://example.com/java-control', 1, 3, 55);

-- Insert sample assignments
INSERT IGNORE INTO Assignments (title, description, courseId, dueDate, maxPoints) VALUES
('Java Basics Quiz', 'Complete the quiz on Java fundamentals covering variables, data types, and basic syntax.', 1, DATE_ADD(NOW(), INTERVAL 7 DAY), 100),
('JavaScript Project', 'Build a simple web application using HTML, CSS, and JavaScript.', 2, DATE_ADD(NOW(), INTERVAL 14 DAY), 200),
('Python Data Analysis', 'Analyze a dataset using Python pandas and create visualizations.', 3, DATE_ADD(NOW(), INTERVAL 10 DAY), 150);

-- Create indexes for better performance on Heroku
CREATE INDEX IF NOT EXISTS idx_users_username ON Users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON Users(email);
CREATE INDEX IF NOT EXISTS idx_courses_category ON Courses(category);
CREATE INDEX IF NOT EXISTS idx_courses_difficulty ON Courses(difficulty);
CREATE INDEX IF NOT EXISTS idx_lectures_course ON Lectures(courseId);
CREATE INDEX IF NOT EXISTS idx_enrollments_student ON Enrollments(studentId);
CREATE INDEX IF NOT EXISTS idx_enrollments_course ON Enrollments(courseId);
CREATE INDEX IF NOT EXISTS idx_assignments_course ON Assignments(courseId);
CREATE INDEX IF NOT EXISTS idx_submissions_assignment ON Submissions(assignmentId);
CREATE INDEX IF NOT EXISTS idx_submissions_student ON Submissions(studentId);
CREATE INDEX IF NOT EXISTS idx_activity_user ON UserActivity(userId);
CREATE INDEX IF NOT EXISTS idx_chat_user ON ChatHistory(userId);
