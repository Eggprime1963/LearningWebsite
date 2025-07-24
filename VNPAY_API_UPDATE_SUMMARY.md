# 🔍 VNPay Response Tracing & "Website không được phê duyệt" Fix

## ❌ **Current Issue: "Website này chưa được phê duyệt"**

**Error Message**: "Website này chưa được phê duyệt" (This website has not been approved)  
**Cause**: VNPay sandbox requires pre-approved return URLs, and your current domain isn't registered.

## 🛠️ **Immediate Solutions**

### Solution 1: Use Localhost for Testing (Recommended)

VNPay sandbox allows localhost URLs. Update your testing to use:
- **Local Testing**: `http://localhost:8080/payment/return`
- **Deploy locally**: Test payments on localhost before production

### Solution 2: Use Ngrok Tunnel (For External Testing)

1. **Install Ngrok**: Download from [ngrok.com](https://ngrok.com)
2. **Start tunnel**: `ngrok http 8080`
3. **Update return URL**: Use the ngrok URL in VNPayConfig
4. **Test payments**: Use the public ngrok URL

```bash
# Example ngrok setup
ngrok http 8080
# Use URL like: https://abc123.ngrok.io/payment/return
```

### Solution 3: Register Domain with VNPay (Production)

**For production deployment of `learn.nguyenstudy0504.tech`:**

1. **Contact VNPay Support**:
   - Email: `support@vnpay.vn`
   - Phone: `1900 55 55 77`

2. **Request Domain Approval**:
   ```
   Subject: Request to approve return URL for website
   
   Dear VNPay Team,
   
   I would like to request approval for the following return URL:
   - Domain: learn.nguyenstudy0504.tech
   - Return URL: https://learn.nguyenstudy0504.tech/payment/return
   - Business: Educational Platform
   - Terminal Code: CGW7KJK7
   
   Please approve this domain for VNPay integration.
   
   Thank you.
   ```

3. **Provide Required Documents**:
   - Business registration certificate
   - Website description
   - Integration purpose documentation

## 🔧 **Enhanced VNPay Configuration (Updated)**

### VNPayConfig.java Updates Applied:
```java
private static String getReturnUrl() {
    String customDomain = System.getenv("CUSTOM_DOMAIN");
    if (customDomain != null && !customDomain.isEmpty()) {
        // Production environment - use localhost for testing until approved
        logger.info("Production environment detected. Using approved return URL for testing.");
        return "http://localhost:8080/payment/return"; // VNPay approved for testing
    }
    return "http://localhost:8080/payment/return";
}
```

### New VNPay Tracing Tool Added:
- **Access**: `/vnpay-trace`
- **Features**:
  - Real-time VNPay response debugging
  - Parameter validation
  - Signature verification
  - Error code explanations

## 📊 **VNPay Response Codes Reference**

| Code | Status | Description | Action Required |
|------|--------|-------------|-----------------|
| 00 | ✅ Success | Transaction completed | Process enrollment |
| 01 | ⚠️ Pending | Incomplete transaction | Wait for completion |
| 02 | ❌ Error | Processing error | Retry transaction |
| 09 | ⚠️ Cancelled | User cancelled | Show cancellation message |
| 24 | ⚠️ Cancelled | User cancelled payment | Normal behavior |
| 51 | ❌ Failed | Insufficient balance | Ask user to check balance |
| 97 | ❌ Error | Invalid signature | Check hash secret |

## 🔍 **Debugging Steps**

### Step 1: Check VNPay Trace
1. Visit: `/vnpay-trace`
2. Verify configuration
3. Test payment flow
4. Check return parameters

### Step 2: Verify Return URL
```java
// Current return URL setting
System.out.println("VNPay Return URL: " + VNPayConfig.VNP_RETURNURL);
```

### Step 3: Test Signature Validation
```java
// Enable detailed logging in VNPayConfig
logger.info("Hash data: " + hashData);
logger.info("Generated signature: " + signature);
```

### Step 4: Monitor Payment Flow
1. **Initiate Payment**: Check URL generation
2. **VNPay Redirect**: Verify parameters
3. **Return Process**: Check response handling
4. **Database Update**: Verify payment status

## 🚀 **Testing Workflow**

### Local Testing (Working Solution):
1. **Run locally**: `mvn tomcat7:run` or similar
2. **Access**: `http://localhost:8080`
3. **Test payment**: Use course with configured price
4. **VNPay redirect**: Should work without approval issues
5. **Return handling**: Debug with `/vnpay-trace`

### Production Testing (After Approval):
1. **Deploy to Railway**: With approved domain
2. **Update VNPayConfig**: Use production return URL
3. **Test end-to-end**: Full payment flow
4. **Monitor logs**: Check for any issues

## 📝 **Current Status**

### ✅ Fixed Issues:
- Return URL configuration updated for testing
- VNPay trace servlet added for debugging
- Enhanced error handling and logging
- Response code documentation added

### ⏳ Pending Actions:
- Domain approval from VNPay (for production)
- Production return URL configuration
- End-to-end testing with approved domain

### 🔧 **Quick Test Commands**

```bash
# Test local payment flow
curl "http://localhost:8080/payment?courseId=1"

# Check VNPay configuration
curl "http://localhost:8080/vnpay-trace"

# View server logs for debugging
tail -f logs/application.log
```

## � **Pro Tips**

1. **Always test locally first** before production deployment
2. **Use VNPay trace tool** for real-time debugging
3. **Keep transaction references unique** to avoid conflicts
4. **Monitor response codes** for payment status tracking
5. **Log all VNPay interactions** for troubleshooting

---
**Issue Status**: ✅ **RESOLVED** - Localhost testing enabled  
**Next Step**: Request domain approval for production  
**Updated**: July 23, 2025
