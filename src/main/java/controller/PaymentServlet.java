package controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.CourseDAO;
import dao.EnrollmentDAO;
import dao.PaymentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Course;
import model.Enrollment;
import model.Payment;
import model.User;
import util.VNPayConfig;

@WebServlet({"/payment", "/payment/return"})
public class PaymentServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(PaymentServlet.class.getName());
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.equals("/return")) {
            handlePaymentReturn(request, response);
        } else {
            showPaymentForm(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("initiate".equals(action)) {
            initiatePayment(request, response);
        } else {
            response.sendRedirect("/");
        }
    }

    private void showPaymentForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("/login");
            return;
        }

        String courseIdParam = request.getParameter("courseId");
        if (courseIdParam == null) {
            response.sendRedirect("/course");
            return;
        }

        try {
            int courseId = Integer.parseInt(courseIdParam);
            Optional<Course> courseOpt = courseDAO.findCourseById(courseId);
            
            if (!courseOpt.isPresent()) {
                response.sendRedirect("/course");
                return;
            }
            
            Course course = courseOpt.get();

            // Get price from VNPayConfig
            // Convert BigDecimal price to int
            Integer price = (course.getPrice() != null) ? course.getPrice().intValue() : null;
            if (price == null || price == 0) {
                // Course not configured for payment or free
                response.sendRedirect("/course");
                return;
            }

            request.setAttribute("course", course);
            request.setAttribute("price", price);
            request.getRequestDispatcher("/WEB-INF/jsp/payment-form.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect("/course");
        }
    }

    private void initiatePayment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }

        String courseIdParam = request.getParameter("courseId");
        if (courseIdParam == null) {
            response.sendRedirect("/course");
            return;
        }

        try {
            int courseId = Integer.parseInt(courseIdParam);
            Optional<Course> courseOpt = courseDAO.findCourseById(courseId);
            
            if (!courseOpt.isPresent()) {
                response.sendRedirect("/course");
                return;
            }
            
            Course course = courseOpt.get();

            // Check if user already paid for this course
            if (paymentDAO.hasUserPaidForCourse(user.getId(), courseId)) {
                response.sendRedirect("/course?message=already_enrolled");
                return;
            }

            Integer amountInt = (course.getPrice() != null) ? course.getPrice().intValue() : null;
            if (amountInt == null || amountInt == 0) {
                response.sendRedirect("/course?message=invalid_course");
                return;
            }

            // VNPay 2.1.0 parameters following official documentation
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_TxnRef = generateTxnRef();
            String vnp_OrderInfo = "Payment for " + course.getName();
            String vnp_OrderType = "other";
            String vnp_Amount = String.valueOf(amountInt * 100); // Convert to VND cents
            String vnp_Locale = "vn";
            String vnp_IpAddr = VNPayConfig.getIpAddress(request);
            
            // Create timestamp
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            
            // Set expiration time (15 minutes)
            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());

            // Build VNPay parameters map following official pattern
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
            vnp_Params.put("vnp_Amount", vnp_Amount);
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", vnp_OrderType);
            vnp_Params.put("vnp_Locale", vnp_Locale);
            vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // Optional: Add billing information if available
            String userEmail = user.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                vnp_Params.put("vnp_Bill_Email", userEmail);
            }

            // Validate all required VNPay parameters are present
            String[] requiredParams = {
                "vnp_Version", "vnp_Command", "vnp_TmnCode", "vnp_Amount", 
                "vnp_CurrCode", "vnp_TxnRef", "vnp_OrderInfo", "vnp_OrderType",
                "vnp_Locale", "vnp_ReturnUrl", "vnp_IpAddr", "vnp_CreateDate"
            };
            
            for (String param : requiredParams) {
                if (!vnp_Params.containsKey(param) || vnp_Params.get(param) == null || vnp_Params.get(param).isEmpty()) {
                    logger.log(Level.SEVERE, "Missing required VNPay parameter: {0}", param);
                    response.sendRedirect("/course?message=payment_config_error");
                    return;
                }
            }
            
            logger.log(Level.INFO, "All required VNPay parameters validated successfully");

            // Build hash data and query string following official VNPay pattern
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Build hash data (no URL encoding for hash)
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(fieldValue);
                    
                    // Build query (with proper VNPay encoding - spaces become + not %20)
                    try {
                        query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                        query.append('=');
                        // VNPay specific: replace %20 with + for proper encoding
                        String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()).replace("%20", "+");
                        query.append(encodedValue);
                    } catch (UnsupportedEncodingException e) {
                        logger.log(Level.SEVERE, "Encoding error for field: " + fieldName, e);
                        // Fallback to non-encoded values
                        query.append(fieldName).append('=').append(fieldValue);
                    }
                    
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            
            String queryUrl = query.toString();
            String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;

            // Comprehensive debug logging for signature troubleshooting
            logger.log(Level.INFO, "=== VNPay Payment Debug Info ===");
            logger.log(Level.INFO, "Transaction Reference: {0}", vnp_TxnRef);
            logger.log(Level.INFO, "Amount (VND cents): {0}", vnp_Amount);
            logger.log(Level.INFO, "IP Address: {0}", vnp_IpAddr);
            logger.log(Level.INFO, "Hash Data (for signature): {0}", hashData.toString());
            logger.log(Level.INFO, "Generated Signature: {0}", vnp_SecureHash);
            logger.log(Level.INFO, "Final Payment URL: {0}", paymentUrl);
            logger.log(Level.INFO, "=== End Debug Info ===");

            // Create payment record with PENDING status
            Payment payment = new Payment();
            payment.setUser(user);
            payment.setCourse(course);
            payment.setVnpAmount(amountInt.longValue());
            payment.setVnpTxnRef(vnp_TxnRef);
            payment.setVnpOrderInfo(vnp_OrderInfo);
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setCreatedDate(LocalDateTime.now());

            paymentDAO.savePayment(payment);
            
            logger.log(Level.INFO, "Initiating VNPay payment for user {0}, course {1}, amount {2}, txnRef {3}", 
                new Object[]{user.getId(), courseId, amountInt, vnp_TxnRef});

            response.sendRedirect(paymentUrl);

        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid course ID format: {0}", courseIdParam);
            response.sendRedirect("/course?message=error");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initiating payment", e);
            response.sendRedirect("/course?message=error");
        }
    }

    private void handlePaymentReturn(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Following official VNPay return handling pattern
            Map<String, String> fields = new HashMap<>();
            
            // Get all VNPay parameters (no URL encoding needed for return processing)
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnpTxnRef = request.getParameter("vnp_TxnRef");
            String vnpResponseCode = request.getParameter("vnp_ResponseCode");
            String vnpTransactionNo = request.getParameter("vnp_TransactionNo");
            String vnpSecureHash = request.getParameter("vnp_SecureHash");

            logger.log(Level.INFO, "Payment return - TxnRef: {0}, ResponseCode: {1}", 
                new Object[]{vnpTxnRef, vnpResponseCode});

            if (vnpTxnRef == null) {
                response.sendRedirect("/course?message=invalid_return");
                return;
            }

            // Find payment record
            Payment payment = paymentDAO.findByVnpTxnRef(vnpTxnRef).orElse(null);
            if (payment == null) {
                response.sendRedirect("/course?message=payment_not_found");
                return;
            }

            // Remove hash parameters for validation (following official VNPay pattern)
            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }

            // Validate signature using VNPay's method
            String signValue = VNPayConfig.hashAllFields(fields);
            boolean isValidSignature = signValue.equals(vnpSecureHash);
            
            // Comprehensive debug logging for return validation
            logger.log(Level.INFO, "=== VNPay Return Debug Info ===");
            logger.log(Level.INFO, "Transaction Reference: {0}", vnpTxnRef);
            logger.log(Level.INFO, "Response Code: {0}", vnpResponseCode);
            logger.log(Level.INFO, "All Return Parameters: {0}", fields.toString());
            logger.log(Level.INFO, "Generated Signature: {0}", signValue);
            logger.log(Level.INFO, "Received Signature: {0}", vnpSecureHash);
            logger.log(Level.INFO, "Signature Valid: {0}", isValidSignature);
            logger.log(Level.INFO, "=== End Return Debug Info ===");

            if (!isValidSignature) {
                logger.log(Level.WARNING, "Invalid VNPay signature for transaction: {0}", vnpTxnRef);
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setVnpResponseCode(vnpResponseCode);
                payment.setUpdatedDate(LocalDateTime.now());
                paymentDAO.updatePayment(payment);
                
                response.sendRedirect("/course?message=invalid_signature");
                return;
            }

            // Check order ID, amount, and status (following official pattern)
            boolean checkOrderId = true; // vnp_TxnRef exists in database
            boolean checkAmount = true;  // Amount validation can be added here
            boolean checkOrderStatus = (payment.getStatus() == Payment.PaymentStatus.PENDING);

            if (checkOrderId && checkAmount && checkOrderStatus) {
                // Update payment status based on response code
                if ("00".equals(vnpResponseCode)) {
                    // Payment successful
                    payment.setStatus(Payment.PaymentStatus.SUCCESS);
                    payment.setVnpTransactionNo(vnpTransactionNo);
                    payment.setVnpResponseCode(vnpResponseCode);
                    payment.setUpdatedDate(LocalDateTime.now());
                    paymentDAO.updatePayment(payment);

                    // Enroll user in course
                    try {
                        Enrollment enrollment = new Enrollment();
                        enrollment.setStudent((User) payment.getUser());
                        enrollment.setCourse(payment.getCourse());
                        enrollment.setEnrollmentDate(LocalDateTime.now());
                        enrollmentDAO.save(enrollment);

                        logger.log(Level.INFO, "User {0} successfully enrolled in course {1} after payment", 
                            new Object[]{payment.getUser().getId(), payment.getCourse().getIdCourse()});

                        response.sendRedirect("/course?message=payment_success");
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error enrolling user after successful payment", e);
                        response.sendRedirect("/course?message=enrollment_error");
                    }
                } else {
                    // Payment failed
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                    payment.setVnpResponseCode(vnpResponseCode);
                    payment.setUpdatedDate(LocalDateTime.now());
                    paymentDAO.updatePayment(payment);

                    logger.log(Level.INFO, "Payment failed for transaction {0} with code {1}", 
                        new Object[]{vnpTxnRef, vnpResponseCode});

                    response.sendRedirect("/course?message=payment_failed");
                }
            } else {
                // Order validation failed
                String message = !checkOrderId ? "order_not_found" : 
                               !checkAmount ? "invalid_amount" : "order_already_confirmed";
                logger.log(Level.WARNING, "Order validation failed for transaction {0}: {1}", 
                    new Object[]{vnpTxnRef, message});
                response.sendRedirect("/course?message=" + message);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing payment return", e);
            response.sendRedirect("/course?message=error");
        }
    }

    private String generateTxnRef() {
        // Generate unique transaction reference
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        SecureRandom random = new SecureRandom();
        int randomNum = random.nextInt(10000);
        return timestamp + String.format("%04d", randomNum);
    }
    
    /**
     * Test method to validate VNPay signature generation
     * This method can be called to verify your signature generation works correctly
     */
    public static void testVNPaySignature() {
        // Sample test data - you can modify this to match your actual payment data
        Map<String, String> testParams = new HashMap<>();
        testParams.put("vnp_Version", "2.1.0");
        testParams.put("vnp_Command", "pay");
        testParams.put("vnp_TmnCode", "CGW7KJK7");
        testParams.put("vnp_Amount", "100000");
        testParams.put("vnp_CurrCode", "VND");
        testParams.put("vnp_TxnRef", "20250723123456789");
        testParams.put("vnp_OrderInfo", "Test payment");
        testParams.put("vnp_OrderType", "other");
        testParams.put("vnp_Locale", "vn");
        testParams.put("vnp_ReturnUrl", "http://localhost:8080/payment/return");
        testParams.put("vnp_IpAddr", "127.0.0.1");
        testParams.put("vnp_CreateDate", "20250723123456");
        
        String signature = VNPayConfig.hashAllFields(testParams);
        System.out.println("Test VNPay Signature: " + signature);
        System.out.println("Using Secret Key: " + VNPayConfig.secretKey);
    }
}
