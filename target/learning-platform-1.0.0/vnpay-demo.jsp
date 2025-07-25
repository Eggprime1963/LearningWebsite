<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
        <meta name="description" content="">
        <meta name="author" content="">
        <title>VNPay Official Demo - Learning Platform</title>
        <!-- Bootstrap core CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <!-- Custom styles for this template -->
        <style>
            body {
                padding-top: 40px;
                padding-bottom: 40px;
                background-color: #eee;
            }

            .form-signin {
                max-width: 500px;
                padding: 15px;
                margin: 0 auto;
                background-color: #fff;
                border-radius: 10px;
                box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            }

            .form-signin .form-signin-heading,
            .form-signin .checkbox {
                margin-bottom: 30px;
            }

            .form-signin .checkbox {
                font-weight: normal;
            }

            .form-signin .form-control {
                position: relative;
                height: auto;
                -webkit-box-sizing: border-box;
                -moz-box-sizing: border-box;
                box-sizing: border-box;
                padding: 10px;
                font-size: 16px;
            }

            .form-signin .form-control:focus {
                z-index: 2;
            }

            .form-signin input[type="email"] {
                margin-bottom: -1px;
                border-bottom-right-radius: 0;
                border-bottom-left-radius: 0;
            }

            .form-signin input[type="password"] {
                margin-bottom: 10px;
                border-top-left-radius: 0;
                border-top-right-radius: 0;
            }
        </style>
    </head>

    <body>
        <div class="container">
            <form class="form-signin" action="/vnpay-ajax" method="post" id="frmCreateOrder">
                <h2 class="form-signin-heading">🏦 VNPay Official Demo</h2>
                <p class="text-muted">Using Official Credentials: CGW7KJK7</p>
                
                <div class="form-group mb-3">
                    <label for="language">Ngôn ngữ</label>
                    <select name="language" id="language" class="form-control">
                        <option value="vn">Tiếng Việt</option>
                        <option value="en">English</option>
                    </select>
                </div>
                
                <div class="form-group mb-3">
                    <label for="bankCode">Ngân hàng</label>
                    <select name="bankCode" id="bankCode" class="form-control">
                        <option value="">Cổng thanh toán VNPAYQR</option>
                        <option value="VNPAYQR">Thanh toán bằng ứng dụng hỗ trợ VNPAYQR</option>
                        <option value="VNBANK">LOCAL BANK</option>
                        <option value="IB">INTERNET BANKING</option>
                        <option value="ATM">ATM CARD</option>
                        <option value="INTCARD">INTERNATIONAL CARD</option>
                        <option value="VISA">VISA</option>
                        <option value="MASTERCARD">MASTERCARD</option>
                        <option value="JCB">JCB</option>
                        <option value="UPI">UPI</option>
                        <option value="VIB">VIB</option>
                        <option value="VIETCOMBANK">VIETCOMBANK</option>
                        <option value="ICB">ICB</option>
                        <option value="TECHCOMBANK">TECHCOMBANK</option>
                        <option value="EXIMBANK">EXIMBANK</option>
                        <option value="MSBANK">MSBANK</option>
                        <option value="NAMABANK">NAMABANK</option>
                        <option value="SACOMBANK">SACOMBANK</option>
                        <option value="BACABANK">BACABANK</option>
                        <option value="AGRIBANK">AGRIBANK</option>
                        <option value="TPBANK">TPBANK</option>
                        <option value="BIDV">BIDV</option>
                        <option value="TECHCOMBANK">TECHCOMBANK</option>
                        <option value="VPBANK">VPBANK</option>
                        <option value="MBBANK">MBBANK</option>
                        <option value="ACB">ACB</option>
                        <option value="OCB">OCB</option>
                        <option value="IVB">IVB</option>
                        <option value="SHB">SHB</option>
                    </select>
                </div>
                
                <div class="form-group mb-3">
                    <label for="amount">Số tiền</label>
                    <input class="form-control" id="amount" name="amount" type="number" value="10000" min="1" max="100000000" required>
                </div>
                
                <div class="form-group mb-3">
                    <label for="orderDescription">Nội dung thanh toán</label>
                    <textarea class="form-control" cols="20" id="orderDescription" name="orderDescription" rows="2" placeholder="Noi dung thanh toan">Thanh toan don hang thoi gian: <%= new java.util.Date() %></textarea>
                </div>
                
                <button class="btn btn-lg btn-primary btn-block" type="submit" id="btnCreateOrder">🚀 Thanh toán ngay</button>
                
                <div class="mt-3">
                    <h6>Demo Information:</h6>
                    <p class="small text-muted">
                        <strong>Terminal ID:</strong> CGW7KJK7<br>
                        <strong>Environment:</strong> Sandbox<br>
                        <strong>Version:</strong> 2.1.0
                    </p>
                </div>
            </form>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        
        <script>
            $("#frmCreateOrder").submit(function (event) {
                event.preventDefault(); // Prevent the form from submitting via the browser
                var form = $(this);
                var url = form.attr('action');
                
                $.ajax({
                    type: "POST",
                    url: url,
                    data: form.serialize(), // serializes the form's elements.
                    success: function (data) {
                        var responseCode = data.code;
                        if (responseCode == "00") {
                            // Success - redirect to VNPay
                            window.open(data.data);
                        } else {
                            alert("Error: " + data.message);
                        }
                    },
                    error: function (xhr, status, error) {
                        alert("Có lỗi xảy ra: " + error);
                    }
                });
            });
        </script>
    </body>
</html>
