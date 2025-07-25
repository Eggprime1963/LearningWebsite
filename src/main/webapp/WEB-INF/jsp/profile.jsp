<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile - LearnHub</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/homePage.css"/>
    <!-- Google Fonts: Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        .profile-container {
            display: flex;
            gap: 2rem;
            margin-top: 3rem;
            flex-wrap: wrap;
        }
        .profile-form-card {
            flex: 1 1 340px;
            background: var(--bs-light, #fff);
            border-radius: 16px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.08);
            padding: 2rem;
            min-width: 320px;
        }
        .profile-form label {
            font-weight: 500;
        }
        .profile-form .form-control, .profile-form .form-select {
            border-radius: 8px;
            margin-bottom: 1rem;
        }
        .profile-form .btn {
            width: 100%;
            font-weight: 600;
            border-radius: 8px;
        }
        .activity-card {
            flex: 1 1 320px;
            min-width: 280px;
            background: var(--bs-light, #fff);
            border-radius: 16px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.08);
            padding: 2rem;
        }
        .activity-calendar {
            display: grid;
            grid-template-columns: 80px repeat(7, 48px); /* 1st col for month, then Sun-Sat */
            gap: 16px;
            align-items: center;
        }
        .month-label {
            grid-column: 1 / 2;
            text-align: left;
            font-weight: bold;
            font-size: 1.1rem;
            color: #007bff;
            align-self: center;
            justify-self: left;
            writing-mode: vertical-lr;
            padding-left: 8px;
            border-left: 4px solid #007bff;
            height: 100%;
        }
        .month-separator {
            grid-column: 1 / span 8;
            border-bottom: 2px solid #bbb;
            margin: 12px 0;
            height: 0;
        }
        .activity-cell {
            width: 48px;
            height: 48px;
            border: 2px solid #222;
            border-radius: 8px;
            background: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 16px;
            position: relative;
            grid-column: auto;
        }
        .activity-cell.active-high { background: #ffcccc; }
        .activity-cell.active-mid { background: #ffffcc; }
        .activity-cell.active-low { background: #ccffcc; }
        .weekday-header {
            font-weight: bold;
            text-align: center;
            grid-row: 1;
        }
        @media (max-width: 900px) {
            .profile-container { flex-direction: column; }
        }
    </style>
</head>
<body>
<jsp:include page="navbar.jsp" />

<div class="container profile-container">
    <div class="profile-form-card">
        <h2 class="mb-4 fw-bold">Profile</h2>
        <form class="profile-form" method="post" action="${pageContext.request.contextPath}/profile">
            <c:if test="${not empty message}">
                <div class="alert alert-info" role="alert">
                    ${message}
                </div>
            </c:if>
            <div class="mb-3">
                <label for="username" class="form-label">Username</label>
                <input type="text" id="username" name="username" class="form-control" value="${username}" required>
            </div>
            <div class="mb-3">
                <label for="email" class="form-label">Email</label>
                <input type="email" id="email" name="email" class="form-control" value="${user.email}" readonly>
            </div>
            <div class="row mb-3">
                <div class="col">
                    <label for="firstName" class="form-label">First Name</label>
                    <input type="text" id="firstName" name="firstName" class="form-control" value="${firstName}">
                </div>
                <div class="col">
                    <label for="lastName" class="form-label">Last Name</label>
                    <input type="text" id="lastName" name="lastName" class="form-control" value="${lastName}">
                </div>
            </div>
            <div class="row mb-3">
                <div class="col"><label for="gender" class="form-label">Your gender is</label></div>
                <div class="col">
                    <div class="btn-group w-100" role="group">
                        <input type="radio" class="btn-check" name="gender" id="male" value="Male"
                            ${gender == 'Male' ? 'checked="checked"' : ''}>
                        <label class="btn btn-outline-primary" for="male">Male</label>
                        <input type="radio" class="btn-check" name="gender" id="female" value="Female"
                            ${gender == 'Female' ? 'checked="checked"' : ''}>
                        <label class="btn btn-outline-primary" for="female">Female</label>
                    </div>
                </div>
            </div>
            <div class="mb-3">
                <label for="phone" class="form-label">Phone</label>
                <input type="text" id="phone" name="phone" class="form-control" value="${phone}">
            </div>
            <div class="mb-3">
                <label for="address" class="form-label">Address</label>
                <input type="text" id="address" name="address" class="form-control" value="${address}">
            </div>
            <div class="mb-3">
                <label for="school" class="form-label">School</label>
                <input type="text" id="school" name="school" class="form-control" value="${school}">
            </div>

            <!-- Update Password (skip for Google users) -->
            <c:if test="${empty user.googleId}">
                <hr>
                <h4 class="mb-3">Update Password</h4>
                <div class="mb-3">
                    <label for="currentPassword" class="form-label">Current Password</label>
                    <input type="password" id="currentPassword" name="currentPassword" class="form-control">
                </div>
                <div class="mb-3">
                    <label for="newPassword" class="form-label">New Password</label>
                    <input type="password" id="newPassword" name="newPassword" class="form-control">
                </div>
                <div class="mb-3">
                    <label for="confirmNewPassword" class="form-label">Confirm New Password</label>
                    <input type="password" id="confirmNewPassword" name="confirmNewPassword" class="form-control">
                </div>
            </c:if>

            <button type="submit" class="btn btn-success mt-2">Update</button>
        </form>
    </div>
</div>
<!-- Debug block: print all key attributes -->
<footer class="text-center mt-5 mb-3" style="color:#888;">copyright text</footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
