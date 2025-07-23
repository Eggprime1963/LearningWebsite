package util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.http.HttpServletRequest;

public class VNPayConfig {
    private static final Logger logger = Logger.getLogger(VNPayConfig.class.getName());

    // VNPay configuration (using sandbox URL with production credentials)
    public static final String VNP_TMNCODE = "CGW7KJK7"; // Your terminal ID
    public static final String VNP_HASHSECRET = "VGTLQQIUPSSO4ERSSAMGVFS5RRSGBEHT"; // Your hash secret
    public static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String VNP_RETURNURL = getReturnUrl();
    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND = "pay";
    public static final String VNP_ORDERTYPE = "other";
    public static final String VNP_CURRCODE = "VND";
    public static final String VNP_LOCALE = "vn";

    // Course prices (in VND)
    public static final Map<Integer, Integer> COURSE_PRICES = new HashMap<Integer, Integer>() {{
        put(9, 299000); // Course ID 9 - 299,000 VND
        put(1, 199000); // Course ID 1 - 199,000 VND  
        put(2, 399000); // Course ID 2 - 399,000 VND
        put(3, 249000); // Course ID 3 - 249,000 VND
    }};

    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue); // No URL encoding for hash data
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        String hashData = sb.toString();
        logger.info("Hash data for signature: " + hashData);
        String signature = hmacSHA512(VNP_HASHSECRET, hashData);
        logger.info("Generated signature: " + signature);
        return signature;
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            logger.severe("Error creating HMAC SHA512: " + ex.getMessage());
            return "";
        }
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            // Check for IP from proxy or load balancer
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("X-Real-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            
            // Handle IPv6 localhost
            if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }
            
            // Take first IP if multiple IPs (comma-separated)
            if (ipAddress != null && ipAddress.contains(",")) {
                ipAddress = ipAddress.split(",")[0].trim();
            }
            
            // Validate IPv4 format (basic validation)
            if (ipAddress != null && !ipAddress.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                logger.warning("Non-IPv4 address detected: " + ipAddress + ", using fallback");
                ipAddress = "127.0.0.1"; // Fallback to localhost
            }
            
        } catch (Exception e) {
            logger.severe("Error getting IP address: " + e.getMessage());
            ipAddress = "127.0.0.1"; // Safe fallback
        }
        
        return ipAddress;
    }

    // Method for PaymentServlet with custom transaction reference
    public static String createPaymentUrlWithTxnRef(int courseId, String courseName, int amount, 
                                        String txnRef, String ipAddr) {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNP_VERSION);
        vnp_Params.put("vnp_Command", VNP_COMMAND);
        vnp_Params.put("vnp_TmnCode", VNP_TMNCODE);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay requires amount * 100
        vnp_Params.put("vnp_CurrCode", VNP_CURRCODE);
        vnp_Params.put("vnp_TxnRef", txnRef); // Use provided transaction reference
        vnp_Params.put("vnp_OrderInfo", "Payment for " + courseName);
        vnp_Params.put("vnp_OrderType", VNP_ORDERTYPE);
        vnp_Params.put("vnp_Locale", VNP_LOCALE);
        vnp_Params.put("vnp_ReturnUrl", VNP_RETURNURL);
        vnp_Params.put("vnp_IpAddr", ipAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Payment expires in 15 minutes
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sort parameters for hash generation
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        
        // Build hash data (without URL encoding) and query string (with URL encoding)
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Hash data - NO URL encoding (as per VNPay specification)
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(fieldValue);
                
                // Query string - WITH URL encoding
                try {
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    // This shouldn't happen with UTF-8
                    e.printStackTrace();
                }
                
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        // Generate secure hash using HMAC SHA512
        String vnp_SecureHash = hmacSHA512(VNP_HASHSECRET, hashData.toString());
        
        // Build final URL
        String queryUrl = query.toString();
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return VNP_URL + "?" + queryUrl;
    }

    public static String createPaymentUrl(int courseId, String courseName, int amount, 
                                        String orderInfo, String ipAddr) {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNP_VERSION);
        vnp_Params.put("vnp_Command", VNP_COMMAND);
        vnp_Params.put("vnp_TmnCode", VNP_TMNCODE);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay requires amount * 100
        vnp_Params.put("vnp_CurrCode", VNP_CURRCODE);
        vnp_Params.put("vnp_TxnRef", getRandomNumber(8));
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", VNP_ORDERTYPE);
        vnp_Params.put("vnp_Locale", VNP_LOCALE);
        vnp_Params.put("vnp_ReturnUrl", VNP_RETURNURL);
        vnp_Params.put("vnp_IpAddr", ipAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sort parameters for hash generation
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        
        // Build hash data (without URL encoding) and query string (with URL encoding)
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Hash data - NO URL encoding (as per VNPay specification)
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(fieldValue);
                
                // Query string - WITH URL encoding
                try {
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    logger.severe("Error encoding URL: " + e.getMessage());
                }
                
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }
        
        // Generate secure hash using HMAC SHA512
        String vnp_SecureHash = hmacSHA512(VNP_HASHSECRET, hashData.toString());
        
        // Build final URL
        String queryUrl = query.toString();
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        
        return VNP_URL + "?" + queryUrl;
    }

    public static boolean validateSignature(Map<String, String> fields, String secureHash) {
        Map<String, String> vnp_Params = new HashMap<>(fields);
        vnp_Params.remove("vnp_SecureHashType");
        vnp_Params.remove("vnp_SecureHash");
        String signValue = hashAllFields(vnp_Params);
        return signValue.equals(secureHash);
    }
    
    /**
     * Get the return URL based on the environment
     * For Heroku deployment, use the app URL, otherwise localhost
     */
    private static String getReturnUrl() {
        String herokuAppUrl = System.getenv("HEROKU_APP_URL");
        if (herokuAppUrl != null && !herokuAppUrl.isEmpty()) {
            return herokuAppUrl + "/payment/return";
        }
        return "http://localhost:8080/payment/return";
    }
}
