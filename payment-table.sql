-- Create Payments table for VNPay integration
-- This table stores payment transaction information for course enrollments

CREATE TABLE IF NOT EXISTS Payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vnp_TxnRef VARCHAR(255) UNIQUE NOT NULL,
    vnp_Amount BIGINT NOT NULL,
    vnp_OrderInfo TEXT,
    vnp_ResponseCode VARCHAR(10),
    vnp_TransactionNo VARCHAR(255),
    vnp_BankCode VARCHAR(20),
    vnp_PayDate VARCHAR(20),
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    payment_status ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_course FOREIGN KEY (course_id) REFERENCES Courses(id) ON DELETE CASCADE,
    
    -- Indexes for better performance
    INDEX idx_payment_user (user_id),
    INDEX idx_payment_course (course_id),
    INDEX idx_payment_status (payment_status),
    INDEX idx_payment_txnref (vnp_TxnRef),
    INDEX idx_payment_created (created_date)
);

-- Add some comments for clarity
ALTER TABLE Payments COMMENT = 'Stores VNPay payment transactions for course enrollments';
