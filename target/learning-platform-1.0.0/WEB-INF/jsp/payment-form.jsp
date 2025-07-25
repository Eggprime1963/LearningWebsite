<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment - Learning Platform</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
        }
        .payment-container {
            max-width: 600px;
            margin: 2rem auto;
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        .payment-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 2rem;
            text-align: center;
        }
        .course-info {
            background: #f8f9fa;
            padding: 1.5rem;
            border-bottom: 1px solid #dee2e6;
        }
        .price-display {
            font-size: 2rem;
            font-weight: bold;
            color: #28a745;
        }
        .vnpay-logo {
            max-height: 60px;
            margin: 1rem 0;
        }
        .btn-pay {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            border: none;
            color: white;
            font-weight: bold;
            padding: 12px 30px;
            border-radius: 25px;
            transition: all 0.3s;
        }
        .btn-pay:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(40, 167, 69, 0.4);
            color: white;
        }
        .security-note {
            background: #e3f2fd;
            border-left: 4px solid #2196f3;
            padding: 1rem;
            margin: 1rem 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="payment-container">
            <!-- Header -->
            <div class="payment-header">
                <h2><i class="bi bi-credit-card me-2"></i>Course Enrollment Payment</h2>
                <p class="mb-0">Secure payment powered by VNPay</p>
            </div>

            <!-- Course Information -->
            <div class="course-info">
                <h4><i class="bi bi-book me-2"></i>${course.name}</h4>
                <p class="text-muted mb-2">${course.description}</p>
                <div class="row">
                    <div class="col-md-6">
                        <strong>Course ID:</strong> ${course.idCourse}
                    </div>
                    <div class="col-md-6 text-end">
                        <span class="price-display">
                            <fmt:formatNumber value="${price}" type="number" groupingUsed="true"/> VND
                        </span>
                    </div>
                </div>
            </div>

            <!-- Payment Form -->
            <div class="p-4">
                <div class="text-center mb-4">
                    <img src="https://pay.vnpay.vn/images/brands/logo-en.svg" alt="VNPay" class="vnpay-logo">
                    <p class="text-muted">You will be redirected to VNPay to complete your payment</p>
                </div>

                <div class="security-note">
                    <h6><i class="bi bi-shield-check me-2"></i>Payment Security</h6>
                    <ul class="mb-0 small">
                        <li>All transactions are encrypted and secure</li>
                        <li>Your payment information is protected</li>
                        <li>SSL encryption ensures data security</li>
                    </ul>
                </div>

                <form action="${pageContext.request.contextPath}/payment" method="post" class="text-center">
                    <input type="hidden" name="action" value="initiate">
                    <input type="hidden" name="courseId" value="${course.idCourse}">
                    
                    <div class="mb-3">
                        <p><strong>Payment Amount:</strong> 
                           <span class="text-success">
                               <fmt:formatNumber value="${price}" type="number" groupingUsed="true"/> VND
                           </span>
                        </p>
                    </div>

                    <button type="submit" class="btn btn-pay btn-lg">
                        <i class="bi bi-credit-card me-2"></i>Pay with VNPay
                    </button>
                </form>

                <div class="text-center mt-3">
                    <a href="${pageContext.request.contextPath}/course" class="btn btn-outline-secondary">
                        <i class="bi bi-arrow-left me-2"></i>Back to Courses
                    </a>
                </div>

                <!-- Payment Information -->
                <div class="mt-4 p-3 bg-light rounded">
                    <h6><i class="bi bi-info-circle me-2"></i>Payment Information</h6>
                    <div class="row small">
                        <div class="col-12">
                            <p class="mb-2"><strong>Accepted Payment Methods:</strong></p>
                            <ul class="mb-0">
                                <li>Domestic ATM/Debit Cards</li>
                                <li>International Visa/Mastercard</li>
                                <li>Mobile Banking (QR Code)</li>
                                <li>E-wallets (VNPay, ZaloPay, etc.)</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
