package controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.VNPayConfig;

/**
 * VNPay Official Demo Return Handler
 * Exact replica of official VNPay demo return processing
 * @author VNPay Demo - Adapted for Learning Platform
 */
@WebServlet("/vnpay-return")
public class VNPayReturnServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Exactly like official VNPay demo
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII.toString());
            String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        
        // Using your official credentials for signature validation
        String signValue = VNPayConfig.hashAllFields(fields);
        
        // Set attributes for the result page
        request.setAttribute("vnp_TxnRef", request.getParameter("vnp_TxnRef"));
        request.setAttribute("vnp_Amount", request.getParameter("vnp_Amount"));
        request.setAttribute("vnp_OrderInfo", request.getParameter("vnp_OrderInfo"));
        request.setAttribute("vnp_ResponseCode", request.getParameter("vnp_ResponseCode"));
        request.setAttribute("vnp_TransactionNo", request.getParameter("vnp_TransactionNo"));
        request.setAttribute("vnp_BankCode", request.getParameter("vnp_BankCode"));
        request.setAttribute("vnp_PayDate", request.getParameter("vnp_PayDate"));
        request.setAttribute("vnp_TransactionStatus", request.getParameter("vnp_TransactionStatus"));
        
        boolean signatureValid = signValue.equals(vnp_SecureHash);
        request.setAttribute("signatureValid", signatureValid);
        
        if (signatureValid) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                request.setAttribute("message", "Giao dịch thành công");
                request.setAttribute("status", "success");
            } else {
                request.setAttribute("message", "Giao dịch không thành công");
                request.setAttribute("status", "failed");
            }
        } else {
            request.setAttribute("message", "Chữ ký không hợp lệ");
            request.setAttribute("status", "invalid");
        }
        
        // Forward to result page
        request.getRequestDispatcher("/vnpay-result.jsp").forward(request, response);
    }
}
