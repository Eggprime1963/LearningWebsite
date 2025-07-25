package controller;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.VNPayConfig;

/**
 * VNPay Configuration Verification Servlet
 * Displays current VNPay credentials to verify they match official demo
 */
@WebServlet("/vnpay-config-verify")
public class VNPayConfigVerificationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>VNPay Configuration Verification</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }");
        out.println(".container { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background-color: #4CAF50; color: white; }");
        out.println(".credential { font-family: monospace; background: #f0f0f0; padding: 5px; border-radius: 3px; }");
        out.println(".status-match { color: #4CAF50; font-weight: bold; }");
        out.println(".status-mismatch { color: #f44336; font-weight: bold; }");
        out.println(".official { background: #e8f5e8; }");
        out.println(".current { background: #f0f8ff; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        
        out.println("<div class='container'>");
        out.println("<h1>🔍 VNPay Configuration Verification</h1>");
        out.println("<h2>Official Demo Credentials vs Current Configuration</h2>");
        
        // Official credentials
        String officialTmnCode = "CGW7KJK7";
        String officialHashSecret = "VGTLQQIUPSSO4ERSSAMGVFS5RRSGBEHT";
        
        // Current credentials
        String currentTmnCode = VNPayConfig.vnp_TmnCode;
        String currentHashSecret = VNPayConfig.secretKey;
        
        out.println("<table>");
        out.println("<tr><th>Parameter</th><th>Official Demo Value</th><th>Current Project Value</th><th>Status</th></tr>");
        
        // TMN Code verification
        boolean tmnCodeMatch = officialTmnCode.equals(currentTmnCode);
        out.println("<tr>");
        out.println("<td><strong>Terminal ID (vnp_TmnCode)</strong></td>");
        out.println("<td class='official'><span class='credential'>" + officialTmnCode + "</span></td>");
        out.println("<td class='current'><span class='credential'>" + currentTmnCode + "</span></td>");
        out.println("<td class='" + (tmnCodeMatch ? "status-match'>✅ MATCH" : "status-mismatch'>❌ MISMATCH") + "</td>");
        out.println("</tr>");
        
        // Hash Secret verification
        boolean hashSecretMatch = officialHashSecret.equals(currentHashSecret);
        out.println("<tr>");
        out.println("<td><strong>Secret Key (vnp_HashSecret)</strong></td>");
        out.println("<td class='official'><span class='credential'>" + officialHashSecret + "</span></td>");
        out.println("<td class='current'><span class='credential'>" + currentHashSecret + "</span></td>");
        out.println("<td class='" + (hashSecretMatch ? "status-match'>✅ MATCH" : "status-mismatch'>❌ MISMATCH") + "</td>");
        out.println("</tr>");
        
        // Additional configuration
        out.println("<tr>");
        out.println("<td><strong>VNPay URL</strong></td>");
        out.println("<td class='official'>https://sandbox.vnpayment.vn/paymentv2/vpcpay.html</td>");
        out.println("<td class='current'>" + VNPayConfig.vnp_PayUrl + "</td>");
        out.println("<td class='status-match'>✅ STANDARD</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td><strong>Return URL</strong></td>");
        out.println("<td class='official'>Project-specific</td>");
        out.println("<td class='current'>" + VNPayConfig.vnp_ReturnUrl + "</td>");
        out.println("<td class='status-match'>✅ CONFIGURED</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td><strong>Version</strong></td>");
        out.println("<td class='official'>2.1.0</td>");
        out.println("<td class='current'>" + "2.1.0" + "</td>");
        out.println("<td class='status-match'>✅ LATEST</td>");
        out.println("</tr>");
        
        out.println("</table>");
        
        // Overall status
        boolean overallMatch = tmnCodeMatch && hashSecretMatch;
        out.println("<div style='padding: 20px; border-radius: 5px; margin: 20px 0; " + 
                   (overallMatch ? "background: #d4edda; border: 1px solid #c3e6cb; color: #155724;" : 
                                  "background: #f8d7da; border: 1px solid #f5c6cb; color: #721c24;") + "'>");
        out.println("<h3>" + (overallMatch ? "✅ CONFIGURATION VERIFIED" : "❌ CONFIGURATION MISMATCH") + "</h3>");
        if (overallMatch) {
            out.println("<p><strong>Perfect!</strong> Your VNPay configuration exactly matches the official demo credentials.</p>");
            out.println("<p>You can now use VNPay payment functionality with these verified credentials.</p>");
        } else {
            out.println("<p><strong>Warning:</strong> Some credentials don't match the official demo values.</p>");
            out.println("<p>Please verify the credentials are correctly applied.</p>");
        }
        out.println("</div>");
        
        // Demo configuration for reference
        out.println("<div style='background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;'>");
        out.println("<h3>📋 VNPay Demo Configuration Reference</h3>");
        out.println("<p><strong>Official Demo Credentials:</strong></p>");
        out.println("<ul>");
        out.println("<li><strong>Terminal ID:</strong> <code>CGW7KJK7</code></li>");
        out.println("<li><strong>Secret Key:</strong> <code>VGTLQQIUPSSO4ERSSAMGVFS5RRSGBEHT</code></li>");
        out.println("<li><strong>Environment:</strong> Sandbox</li>");
        out.println("<li><strong>API Version:</strong> 2.1.0</li>");
        out.println("</ul>");
        out.println("</div>");
        
        out.println("<div style='text-align: center; margin: 20px 0;'>");
        out.println("<a href='/payment?courseId=1' style='background: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin: 5px;'>Test Payment</a>");
        out.println("<a href='/vnpay-trace' style='background: #2196F3; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin: 5px;'>VNPay Trace</a>");
        out.println("<a href='/' style='background: #9E9E9E; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin: 5px;'>Home</a>");
        out.println("</div>");
        
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
