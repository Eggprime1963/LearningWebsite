package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.VNPayConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * VNPay Response Tracing Servlet
 * Helps debug VNPay payment responses and approval issues
 * Access via: /vnpay-trace
 */
@WebServlet("/vnpay-trace")
public class VNPayTraceServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(VNPayTraceServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>VNPay Response Tracer</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println(".success { color: green; }");
        out.println(".error { color: red; }");
        out.println(".warning { color: orange; }");
        out.println(".info { color: blue; }");
        out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
        out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        out.println("th { background-color: #f2f2f2; }");
        out.println("pre { background: #f5f5f5; padding: 10px; border-radius: 5px; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        
        out.println("<h1>🔍 VNPay Response Tracer</h1>");
        
        // Display current VNPay configuration
        out.println("<h2>📋 Current VNPay Configuration</h2>");
        out.println("<table>");
        out.println("<tr><th>Parameter</th><th>Value</th><th>Status</th></tr>");
        out.println("<tr><td>VNP_TMNCODE</td><td>" + VNPayConfig.VNP_TMNCODE + "</td><td class='info'>✓ Configured</td></tr>");
        out.println("<tr><td>VNP_URL</td><td>" + VNPayConfig.VNP_URL + "</td><td class='info'>✓ Sandbox</td></tr>");
        out.println("<tr><td>VNP_RETURNURL</td><td>" + VNPayConfig.VNP_RETURNURL + "</td><td class='warning'>⚠️ Check Approval</td></tr>");
        out.println("<tr><td>VNP_VERSION</td><td>" + VNPayConfig.VNP_VERSION + "</td><td class='info'>✓ Latest</td></tr>");
        out.println("</table>");
        
        // Check if this is a VNPay return call
        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String vnpTxnRef = request.getParameter("vnp_TxnRef");
        
        if (vnpResponseCode != null || vnpTxnRef != null) {
            out.println("<h2>🔄 VNPay Return Response Detected</h2>");
            
            // Parse all VNPay parameters
            Map<String, String> vnpParams = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String paramName = params.nextElement();
                String paramValue = request.getParameter(paramName);
                if (paramName.startsWith("vnp_")) {
                    vnpParams.put(paramName, paramValue);
                }
            }
            
            // Display all VNPay parameters
            out.println("<table>");
            out.println("<tr><th>VNPay Parameter</th><th>Value</th><th>Description</th></tr>");
            
            for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
                String param = entry.getKey();
                String value = entry.getValue();
                String description = getParameterDescription(param);
                String status = getParameterStatus(param, value);
                
                out.println("<tr><td>" + param + "</td><td>" + value + "</td><td>" + description + " " + status + "</td></tr>");
            }
            out.println("</table>");
            
            // Analyze response code
            if (vnpResponseCode != null) {
                out.println("<h3>📊 Response Code Analysis</h3>");
                String responseDescription = getResponseCodeDescription(vnpResponseCode);
                String responseClass = getResponseClass(vnpResponseCode);
                out.println("<p class='" + responseClass + "'><strong>Code " + vnpResponseCode + "</strong>: " + responseDescription + "</p>");
            }
            
            // Signature validation
            if (vnpParams.containsKey("vnp_SecureHash")) {
                out.println("<h3>🔐 Signature Validation</h3>");
                
                Map<String, String> fieldsForHash = new HashMap<>(vnpParams);
                fieldsForHash.remove("vnp_SecureHash");
                fieldsForHash.remove("vnp_SecureHashType");
                
                String calculatedHash = VNPayConfig.hashAllFields(fieldsForHash);
                String receivedHash = vnpParams.get("vnp_SecureHash");
                
                out.println("<p><strong>Calculated Hash:</strong> " + calculatedHash + "</p>");
                out.println("<p><strong>Received Hash:</strong> " + receivedHash + "</p>");
                
                if (calculatedHash.equals(receivedHash)) {
                    out.println("<p class='success'>✅ Signature is valid</p>");
                } else {
                    out.println("<p class='error'>❌ Signature is invalid</p>");
                }
            }
            
        } else {
            out.println("<h2>📝 VNPay Integration Test</h2>");
            out.println("<p>No VNPay return parameters detected. This page can be used to:</p>");
            out.println("<ul>");
            out.println("<li>Test VNPay configuration</li>");
            out.println("<li>Debug payment return responses</li>");
            out.println("<li>Verify signature validation</li>");
            out.println("</ul>");
            
            out.println("<h3>🔧 Common Issues & Solutions</h3>");
            out.println("<div style='background: #f0f8ff; padding: 15px; border-radius: 5px;'>");
            out.println("<h4>❌ \"Website này chưa được phê duyệt\"</h4>");
            out.println("<p><strong>Problem:</strong> Return URL not approved by VNPay</p>");
            out.println("<p><strong>Solutions:</strong></p>");
            out.println("<ol>");
            out.println("<li>Use localhost for testing: <code>http://localhost:8080/payment/return</code></li>");
            out.println("<li>Contact VNPay support to approve your domain</li>");
            out.println("<li>Use ngrok tunnel for testing: <code>https://abc123.ngrok.io/payment/return</code></li>");
            out.println("</ol>");
            out.println("</div>");
            
            out.println("<div style='background: #fff8f0; padding: 15px; border-radius: 5px; margin-top: 15px;'>");
            out.println("<h4>⚠️ Response Code 24</h4>");
            out.println("<p><strong>Problem:</strong> Transaction cancelled by user</p>");
            out.println("<p><strong>Solution:</strong> Normal behavior when user cancels payment</p>");
            out.println("</div>");
            
            out.println("<div style='background: #f8f0f0; padding: 15px; border-radius: 5px; margin-top: 15px;'>");
            out.println("<h4>❌ Invalid Signature</h4>");
            out.println("<p><strong>Problem:</strong> Hash secret mismatch</p>");
            out.println("<p><strong>Solution:</strong> Verify VNP_HASHSECRET in VNPayConfig.java</p>");
            out.println("</div>");
        }
        
        out.println("<hr>");
        out.println("<p><a href='/payment?courseId=1'>← Test Payment Flow</a> | <a href='/'>Home</a></p>");
        out.println("</body>");
        out.println("</html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Handle POST requests the same way (VNPay might use POST for returns)
        doGet(request, response);
    }
    
    private String getParameterDescription(String param) {
        switch (param) {
            case "vnp_ResponseCode": return "Payment response code";
            case "vnp_TxnRef": return "Transaction reference";
            case "vnp_TransactionNo": return "VNPay transaction number";
            case "vnp_Amount": return "Payment amount (x100)";
            case "vnp_OrderInfo": return "Order description";
            case "vnp_PayDate": return "Payment completion time";
            case "vnp_SecureHash": return "Security signature";
            case "vnp_TmnCode": return "Terminal/Merchant code";
            case "vnp_BankCode": return "Bank code used for payment";
            case "vnp_CardType": return "Card type (ATM/VISA/MASTER)";
            default: return "VNPay parameter";
        }
    }
    
    private String getParameterStatus(String param, String value) {
        if ("vnp_ResponseCode".equals(param)) {
            return "00".equals(value) ? "<span class='success'>✅ Success</span>" : 
                   "<span class='error'>❌ Failed</span>";
        }
        return "";
    }
    
    private String getResponseCodeDescription(String code) {
        switch (code) {
            case "00": return "Transaction successful";
            case "01": return "Incomplete transaction (user didn't complete payment)";
            case "02": return "Error occurred during processing";
            case "04": return "Transaction reversed (refunded)";
            case "05": return "VNPay is processing the transaction (try again later)";
            case "06": return "VNPay has sent refund request to bank";
            case "07": return "Transaction suspected of fraud";
            case "09": return "Customer cancelled transaction";
            case "10": return "Customer unable to complete transaction";
            case "11": return "Transaction expired";
            case "12": return "Bank account locked";
            case "24": return "Transaction cancelled by user";
            case "51": return "Insufficient account balance";
            case "65": return "Exceeded daily transaction limit";
            case "75": return "Bank is under maintenance";
            case "97": return "Invalid signature (checksum error)";
            default: return "Unknown response code: " + code;
        }
    }
    
    private String getResponseClass(String code) {
        return "00".equals(code) ? "success" : "error";
    }
}
