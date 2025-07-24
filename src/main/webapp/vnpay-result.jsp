<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*" %>
<%@page import="java.text.NumberFormat" %>
<%@page import="java.util.Locale" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="description" content="">
        <meta name="author" content="">
        <title>VNPay Return - Learning Platform</title>
        <!-- Bootstrap core CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <!-- Custom styles -->
        <style>
            body {
                padding-top: 40px;
                padding-bottom: 40px;
                background-color: #eee;
            }
            .result-container {
                max-width: 800px;
                margin: 0 auto;
                background-color: #fff;
                border-radius: 10px;
                box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                padding: 30px;
            }
            .status-success { color: #28a745; }
            .status-failed { color: #dc3545; }
            .status-invalid { color: #ffc107; }
        </style>
    </head>

    <body>
        <div class="container">
            <div class="result-container">
                <%
                    String status = (String) request.getAttribute("status");
                    String message = (String) request.getAttribute("message");
                    Boolean signatureValid = (Boolean) request.getAttribute("signatureValid");
                    
                    String vnp_TxnRef = (String) request.getAttribute("vnp_TxnRef");
                    String vnp_Amount = (String) request.getAttribute("vnp_Amount");
                    String vnp_OrderInfo = (String) request.getAttribute("vnp_OrderInfo");
                    String vnp_ResponseCode = (String) request.getAttribute("vnp_ResponseCode");
                    String vnp_TransactionNo = (String) request.getAttribute("vnp_TransactionNo");
                    String vnp_BankCode = (String) request.getAttribute("vnp_BankCode");
                    String vnp_PayDate = (String) request.getAttribute("vnp_PayDate");
                    String vnp_TransactionStatus = (String) request.getAttribute("vnp_TransactionStatus");
                %>
                
                <h2 class="text-center mb-4">🧾 VNPay Payment Result</h2>
                
                <!-- Status Message -->
                <div class="alert alert-<%= "success".equals(status) ? "success" : ("failed".equals(status) ? "danger" : "warning") %> text-center">
                    <h4 class="alert-heading">
                        <%= "success".equals(status) ? "✅" : ("failed".equals(status) ? "❌" : "⚠️") %>
                        <%= message %>
                    </h4>
                </div>
                
                <!-- Transaction Details -->
                <div class="row">
                    <div class="col-md-12">
                        <h5>📋 Chi tiết giao dịch</h5>
                        <table class="table table-bordered">
                            <tr>
                                <td><strong>Mã giao dịch thanh toán:</strong></td>
                                <td><%= vnp_TxnRef != null ? vnp_TxnRef : "N/A" %></td>
                            </tr>
                            <tr>
                                <td><strong>Số tiền:</strong></td>
                                <td>
                                    <%
                                        if (vnp_Amount != null) {
                                            try {
                                                long amount = Long.parseLong(vnp_Amount) / 100;
                                                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                                                out.print(formatter.format(amount).replace("₫", "VND"));
                                            } catch (Exception e) {
                                                out.print(vnp_Amount);
                                            }
                                        } else {
                                            out.print("N/A");
                                        }
                                    %>
                                </td>
                            </tr>
                            <tr>
                                <td><strong>Nội dung thanh toán:</strong></td>
                                <td><%= vnp_OrderInfo != null ? vnp_OrderInfo : "N/A" %></td>
                            </tr>
                            <tr>
                                <td><strong>Mã phản hồi:</strong></td>
                                <td>
                                    <span class="badge bg-<%= "00".equals(vnp_ResponseCode) ? "success" : "danger" %>">
                                        <%= vnp_ResponseCode != null ? vnp_ResponseCode : "N/A" %>
                                        <%= "00".equals(vnp_ResponseCode) ? " (Thành công)" : " (Thất bại)" %>
                                    </span>
                                </td>
                            </tr>
                            <tr>
                                <td><strong>Mã giao dịch tại VNPay:</strong></td>
                                <td><%= vnp_TransactionNo != null ? vnp_TransactionNo : "N/A" %></td>
                            </tr>
                            <tr>
                                <td><strong>Mã ngân hàng thanh toán:</strong></td>
                                <td><%= vnp_BankCode != null ? vnp_BankCode : "N/A" %></td>
                            </tr>
                            <tr>
                                <td><strong>Thời gian thanh toán:</strong></td>
                                <td><%= vnp_PayDate != null ? vnp_PayDate : "N/A" %></td>
                            </tr>
                            <tr>
                                <td><strong>Tình trạng giao dịch:</strong></td>
                                <td>
                                    <span class="badge bg-<%= "00".equals(vnp_TransactionStatus) ? "success" : "danger" %>">
                                        <%= vnp_TransactionStatus != null ? vnp_TransactionStatus : "N/A" %>
                                        <%= "00".equals(vnp_TransactionStatus) ? " (Thành công)" : " (Thất bại)" %>
                                    </span>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
                
                <!-- Signature Validation -->
                <div class="row mt-4">
                    <div class="col-md-12">
                        <h5>🔐 Xác thực chữ ký</h5>
                        <div class="alert alert-<%= signatureValid ? "success" : "danger" %>">
                            <strong>Kết quả xác thực:</strong>
                            <%= signatureValid ? "✅ Chữ ký hợp lệ" : "❌ Chữ ký không hợp lệ" %>
                            <br>
                            <small>Sử dụng Terminal ID: CGW7KJK7</small>
                        </div>
                    </div>
                </div>
                
                <!-- Actions -->
                <div class="text-center mt-4">
                    <a href="/vnpay-demo.jsp" class="btn btn-primary">
                        🔄 Thử giao dịch khác
                    </a>
                    <a href="/" class="btn btn-secondary">
                        🏠 Về trang chủ
                    </a>
                    <a href="/payment?courseId=1" class="btn btn-success">
                        🎓 Thanh toán khóa học
                    </a>
                </div>
                
                <!-- Debug Information -->
                <div class="mt-4">
                    <details>
                        <summary class="text-muted">🔧 Debug Information (Click to expand)</summary>
                        <div class="mt-2">
                            <pre class="bg-light p-3 rounded">
Status: <%= status %>
Message: <%= message %>
Signature Valid: <%= signatureValid %>
Response Code: <%= vnp_ResponseCode %>
Transaction Status: <%= vnp_TransactionStatus %>
Terminal ID Used: CGW7KJK7
                            </pre>
                        </div>
                    </details>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
