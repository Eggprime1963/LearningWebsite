-- Heroku Database Initialization Script
-- This script creates the necessary tables and inserts sample data
-- Designed to work with ClearDB MySQL on Heroku

-- Drop existing tables if they exist (in correct order due to foreign key constraints)
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `submissions`;
DROP TABLE IF EXISTS `user_activity`;
DROP TABLE IF EXISTS `useractivity`;
DROP TABLE IF EXISTS `enrollments`;
DROP TABLE IF EXISTS `assignments`;
DROP TABLE IF EXISTS `lectures`;
DROP TABLE IF EXISTS `coursemetadata`;
DROP TABLE IF EXISTS `courses`;
DROP TABLE IF EXISTS `users`;

-- Create users table
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  `google_id` varchar(255) DEFAULT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `date_of_birth` datetime(6) DEFAULT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `school` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `google_id` (`google_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create courses table with price field
CREATE TABLE `courses` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text,
  `teacher_id` int DEFAULT NULL,
  `image` varchar(127) DEFAULT NULL,
  `difficulty` enum('beginner','intermediate','advanced') DEFAULT 'beginner',
  `category` varchar(50) DEFAULT NULL,
  `lecture_count` int DEFAULT '0',
  `price` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `teacher_id` (`teacher_id`),
  KEY `idx_courses_difficulty` (`difficulty`),
  KEY `idx_courses_category` (`category`),
  CONSTRAINT `courses_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create remaining tables to match the main schema

CREATE TABLE `assignments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `course_id` int DEFAULT NULL,
  `lecture_id` int DEFAULT NULL,
  `title` varchar(100) NOT NULL,
  `description` text,
  `due_date` datetime NOT NULL,
  `status` varchar(100) DEFAULT 'pending',
  PRIMARY KEY (`id`),
  KEY `course_id` (`course_id`),
  KEY `lecture_id` (`lecture_id`),
  CONSTRAINT `assignments_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`),
  CONSTRAINT `assignments_ibfk_2` FOREIGN KEY (`lecture_id`) REFERENCES `lectures` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `lectures` (
  `id` int NOT NULL AUTO_INCREMENT,
  `course_id` int DEFAULT NULL,
  `title` varchar(100) NOT NULL,
  `video_url` varchar(255) NOT NULL,
  `status` varchar(100) DEFAULT 'pending',
  `content` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `course_id` (`course_id`),
  CONSTRAINT `lectures_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `enrollments` (
  `student_id` int NOT NULL,
  `course_id` int NOT NULL,
  `enrollment_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(100) DEFAULT 'active',
  PRIMARY KEY (`student_id`,`course_id`),
  KEY `course_id` (`course_id`),
  CONSTRAINT `enrollments_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`),
  CONSTRAINT `enrollments_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `coursemetadata` (
  `id` int NOT NULL AUTO_INCREMENT,
  `course_id` int NOT NULL,
  `estimated_hours` int NOT NULL DEFAULT '0',
  `learning_outcomes` text,
  `prerequisites` text,
  `skill_level` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_e1o79kv0xbrn75unh2r8kknar` (`course_id`),
  CONSTRAINT `FKetteyt2vjjinwm4ybqvwfc953` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `payments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `vnp_TxnRef` varchar(255) NOT NULL,
  `vnp_Amount` bigint NOT NULL,
  `vnp_OrderInfo` text,
  `vnp_ResponseCode` varchar(10) DEFAULT NULL,
  `vnp_TransactionNo` varchar(255) DEFAULT NULL,
  `vnp_BankCode` varchar(20) DEFAULT NULL,
  `vnp_PayDate` varchar(20) DEFAULT NULL,
  `user_id` int NOT NULL,
  `course_id` int NOT NULL,
  `payment_status` enum('PENDING','SUCCESS','FAILED','CANCELLED') DEFAULT 'PENDING',
  `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `vnp_TxnRef` (`vnp_TxnRef`),
  KEY `idx_payment_user` (`user_id`),
  KEY `idx_payment_course` (`course_id`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_payment_created` (`created_date`),
  CONSTRAINT `fk_payment_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_payment_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `submissions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `assignment_id` int DEFAULT NULL,
  `student_id` int DEFAULT NULL,
  `file_url` varchar(255) DEFAULT NULL,
  `submission_date` datetime DEFAULT NULL,
  `grade` double DEFAULT NULL,
  `lecture_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `assignment_id` (`assignment_id`),
  KEY `student_id` (`student_id`),
  KEY `FKt9l6kbbdem7sw7e6fprs48kag` (`lecture_id`),
  CONSTRAINT `FKt9l6kbbdem7sw7e6fprs48kag` FOREIGN KEY (`lecture_id`) REFERENCES `lectures` (`id`),
  CONSTRAINT `submissions_ibfk_1` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`),
  CONSTRAINT `submissions_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_activity` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `login_time` datetime NOT NULL,
  `logout_time` datetime DEFAULT NULL,
  `duration_minutes` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `user_activity_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Insert sample data
INSERT INTO `users` VALUES 
(1,'admin','$2a$10$3zHz9B..F4T7Y3b2fG7q5e6J7Qz4z2b5q8L9W4m2T8F7Y3N4M6K2','admin@example.com','admin',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(2,'teacher1','$2a$10$3zHz9B..F4T7Y3b2fG7q5e6J7Qz4z2b5q8L9W4m2T8F7Y3N4M6K2','teacher1@example.com','teacher',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(3,'student1','$2a$10$3zHz9B..F4T7Y3b2fG7q5e6J7Qz4z2b5q8L9W4m2T8F7Y3N4M6K2','student1@example.com','student',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO `courses` VALUES 
(1,'Python Basics','Introduction to Python programming',2,'image/python.png','beginner','Programming',12,0.00),
(2,'Java Fundamentals','Core concepts of Java',2,'image/javaf.png','beginner','Programming',10,0.00),
(3,'C++ Programming','Advanced C++ techniques',2,'image/c.png','intermediate','Programming',14,0.00),
(4,'JavaScript Essentials','Basics of JavaScript',2,'image/js.png','beginner','Web Development',8,0.00),
(5,'Ruby on Rails','Web development with Ruby',2,'image/ra.png','intermediate','Web Development',16,0.00),
(6,'PHP Basics','Introduction to PHP',2,'image/php.png','beginner','Web Development',9,0.00),
(7,'Go Programming','Learning Go language',2,'image/go.png','intermediate','Programming',11,0.00),
(8,'Swift for iOS','iOS development with Swift',2,'image/swift.png','intermediate','Mobile Development',18,0.00),
(9,'Advanced Machine Learning with Python','Master advanced ML algorithms, deep learning, and AI applications with hands-on projects. Expert-level course with premium content including neural networks, computer vision, and NLP.',2,'image/python.png','advanced','Machine Learning',25,299000.00),
(10,'Rust Programming','Safe and concurrent Rust',2,'image/Rus.png','advanced','Programming',15,0.00);

INSERT INTO `lectures` VALUES 
(1,1,'Introduction to Python','https://www.youtube.com/watch?v=kqtD5dpn9C8','active','Learn Python programming fundamentals including variables, data types, control structures, and functions. Perfect for beginners starting their programming journey.'),
(2,2,'Java Basics','https://www.youtube.com/watch?v=RRubcjpTkks','active','Master Java programming basics including object-oriented programming concepts, classes, methods, and Java syntax fundamentals.');

-- Show completion message
SELECT 'Database initialization completed successfully!' AS MESSAGE;

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
CREATE INDEX IF NOT EXISTS idx_payment_user ON Payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_course ON Payments(course_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON Payments(payment_status);
CREATE INDEX IF NOT EXISTS idx_payment_txnref ON Payments(vnp_TxnRef);
CREATE INDEX IF NOT EXISTS idx_payment_created ON Payments(created_date);
