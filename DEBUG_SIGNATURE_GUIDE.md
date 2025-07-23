# VNPay Signature Debug Guide

## Date: July 23, 2025

## 🔍 **SIGNATURE VALIDATION ISSUE DIAGNOSIS**

The "wrong signature" error in VNPay typically occurs due to these common issues:

### 1. **Hash Data Encoding Issues**
✅ **FIXED**: Updated hash generation to NOT use URL encoding in hash data (as per VNPay spec)

### 2. **Parameter Filtering Issues**
✅ **FIXED**: Updated PaymentServlet to properly filter VNPay return parameters:
- Include only parameters starting with "vnp_"
- Exclude "vnp_SecureHash" and "vnp_SecureHashType"

### 3. **Debug Information Added**
✅ **ADDED**: Debug logging to see actual hash data and signatures generated

## 🛠 **TROUBLESHOOTING STEPS**

### Step 1: Check Application Logs
After a payment attempt, check the logs for:
```
Hash data for signature: vnp_Amount=29900000&vnp_BankCode=...
Generated signature: abc123def456...
VNPay parameters received: {vnp_Amount=29900000, ...}
VNPay SecureHash received: xyz789...
```

### Step 2: Common Hash Data Issues

#### ❌ **Wrong (causes signature mismatch):**
```
vnp_Amount=299000.00&vnp_Command=pay&...  // Decimal format
vnp_OrderInfo=Payment+for+Course&...      // URL encoded in hash
```

#### ✅ **Correct:**
```
vnp_Amount=29900000&vnp_Command=pay&...   // Integer format (amount * 100)
vnp_OrderInfo=Payment for Course&...      // Raw text in hash
```

### Step 3: Parameter Order
VNPay requires alphabetical parameter sorting:
```
vnp_Amount → vnp_BankCode → vnp_Command → vnp_CreateDate → ...
```

### Step 4: Verify Credentials
Ensure these are correct in VNPayConfig.java:
- `VNP_TMNCODE = "CGW7KJK7"`
- `VNP_HASHSECRET = "VGTLQQIUPSSO4ERSSAMGVFS5RRSGBEHT"`

## 🔧 **SIGNATURE GENERATION PROCESS**

### For Payment URL Creation:
1. **Build Parameters** → Sort alphabetically
2. **Create Hash Data** → Raw values (no URL encoding)
3. **Generate HMAC SHA512** → Using hash secret
4. **Build URL** → URL encode parameters for transmission

### For Return Validation:
1. **Extract Parameters** → Only vnp_* parameters (except vnp_SecureHash)
2. **Sort Parameters** → Alphabetical order
3. **Build Hash String** → Raw values (no decoding needed)
4. **Generate Hash** → Compare with received vnp_SecureHash

## 🎯 **MANUAL TEST PROCEDURE**

### Test Payment Flow:
1. **Initiate Payment** → Check logs for outgoing signature
2. **Complete Payment** → Use VNPay sandbox
3. **Return Processing** → Check logs for validation

### Expected Log Output:
```
INFO: Initiating payment for user 1, course 9, amount 299000
INFO: Hash data for signature: vnp_Amount=29900000&vnp_Command=pay&...
INFO: Generated signature: [64-character hex string]
INFO: Payment return - TxnRef: 202507231234567890, ResponseCode: 00
INFO: VNPay parameters received: {vnp_Amount=29900000, vnp_BankCode=NCB, ...}
INFO: VNPay SecureHash received: [64-character hex string]
```

## 🚨 **COMMON SIGNATURE ISSUES & FIXES**

### Issue 1: Amount Format
- **Problem**: Using decimal amounts in hash
- **Fix**: Always use integer (amount * 100)

### Issue 2: URL Encoding in Hash
- **Problem**: URL encoding values in hash data
- **Fix**: Use raw values for hash, URL encode only for query string

### Issue 3: Parameter Inclusion
- **Problem**: Including non-vnp parameters or wrong exclusions
- **Fix**: Only include vnp_* parameters, exclude vnp_SecureHash/vnp_SecureHashType

### Issue 4: Character Encoding
- **Problem**: UTF-8 encoding issues
- **Fix**: Ensure consistent UTF-8 handling throughout

## 📝 **NEXT STEPS**

1. **Deploy Updated Code** → With debug logging enabled
2. **Test Payment** → Check logs for signature details
3. **Compare Signatures** → Our generated vs VNPay's returned
4. **Verify Hash Data** → Check parameter order and values

If signature still fails after these fixes, the debug logs will show exactly where the mismatch occurs.
