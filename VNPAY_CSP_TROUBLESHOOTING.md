# VNPay CSP and Error Troubleshooting Guide

## 🚨 Content Security Policy (CSP) Console Warnings

### Problem Description
When users are redirected to VNPay's payment page and encounter errors, you may see these browser console warnings:

```
/paymentv2/Payment/Error.html?code=71:1  The Content-Security-Policy directive 'default-src' contains 'style-src' as a source expression. Did you want to add it as a directive and forget a semicolon?
/paymentv2/Payment/Error.html?code=71:1  The Content-Security-Policy directive 'default-src' contains 'img-src' as a source expression. Did you want to add it as a directive and forget a semicolon?
```

### Root Cause
VNPay's server sends **malformed Content-Security-Policy headers**. The correct CSP syntax should be:
```
Content-Security-Policy: default-src 'self'; style-src 'unsafe-inline'; img-src data:
```

But VNPay sends:
```
Content-Security-Policy: default-src 'self' style-src 'unsafe-inline' img-src data:
```
*(Missing semicolons between directives)*

### Key Points
- ✅ **These are VNPay's server-side issues, NOT your application problems**
- ✅ **Safe to ignore** - they don't affect payment functionality
- ⚠️ **Cannot be fixed on your end** - this requires VNPay to fix their server configuration

---

## 🔴 Error Code 71: Website Not Approved

### Problem
Users see: **"Website này chưa được phê duyệt"** (This website is not approved)

### Solutions

#### 1. For Development/Testing
```java
// In VNPayConfig.java - getReturnUrl() method
return "http://localhost:8080/payment/return"; // VNPay allows localhost for testing
```

#### 2. Using ngrok for Testing
```bash
# Install ngrok
npm install -g ngrok

# Create tunnel
ngrok http 8080

# Use the ngrok URL in return URL
return "https://abc123.ngrok.io/payment/return";
```

#### 3. Production Domain Approval
Contact VNPay support to approve your production domain:
- **Email**: support@vnpay.vn
- **Domain to approve**: learn.nguyenstudy0504.tech
- **Return URL**: https://learn.nguyenstudy0504.tech/payment/return

---

## 🛠️ Debugging Tools

### 1. VNPay Trace Servlet
Access: `http://localhost:8080/vnpay-trace`

Features:
- ✅ Display current VNPay configuration
- ✅ Parse VNPay return parameters
- ✅ Validate payment signatures
- ✅ Analyze response codes
- ✅ Troubleshooting guides

### 2. Database Test Servlet
Access: `http://localhost:8080/db-test`

Features:
- ✅ Test database connectivity
- ✅ Verify environment variables
- ✅ Check connection pooling

---

## 📋 VNPay Response Codes

| Code | Description | Action Required |
|------|-------------|-----------------|
| 00 | Success | ✅ Payment completed |
| 24 | Customer cancelled | ⚠️ Normal user behavior |
| 71 | Domain not approved | 🔴 Contact VNPay support |
| 75 | Bank is under maintenance | ⏳ Retry later |
| 79 | Transaction amount exceeds limit | 💰 Reduce amount |

---

## 🔧 Current Configuration

### Return URL Strategy
```java
private static String getReturnUrl() {
    String customDomain = System.getenv("CUSTOM_DOMAIN");
    if (customDomain != null && !customDomain.isEmpty()) {
        // Production: Use localhost for testing until domain approved
        return "http://localhost:8080/payment/return";
    }
    return "http://localhost:8080/payment/return";
}
```

### VNPay Credentials
- **Terminal ID**: CGW7KJK7
- **Environment**: Sandbox
- **Hash Secret**: Configured ✅
- **Version**: 2.1.0

---

## 🚀 Next Steps

1. **Immediate**: Continue testing with localhost URLs
2. **Short-term**: Submit domain approval request to VNPay
3. **Production**: Update return URL once domain is approved

### Domain Approval Request Template
```
Subject: Domain Approval Request for VNPay Integration

Dear VNPay Support,

I would like to request approval for the following domain for VNPay payment integration:

- Domain: learn.nguyenstudy0504.tech
- Return URL: https://learn.nguyenstudy0504.tech/payment/return
- Terminal ID: CGW7KJK7
- Business Type: Educational Platform
- Purpose: Online course payment processing

Please approve this domain for production use.

Thank you.
```

---

## 📞 Support Contacts

- **VNPay Support**: support@vnpay.vn
- **VNPay Documentation**: https://sandbox.vnpayment.vn/apis/
- **Integration Guide**: https://sandbox.vnpayment.vn/apis/vnpay-integration-guide

---

*Last Updated: January 2025*
*Status: CSP warnings documented, Error Code 71 resolution in progress*
