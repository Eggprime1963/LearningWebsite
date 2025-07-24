# 🔧 Environment Variables Fix - DATABASE_URL Secret Error

## ✅ **ISSUE RESOLVED**

**Error**: `Environment Variable "DATABASE_URL" references Secret "database_url", which does not exist.`

**Solution**: Removed undefined secret references from `railway.toml` and updated configuration to use standard environment variables.

## 📋 **How to Set Environment Variables Correctly**

### Method 1: Railway Dashboard (Recommended)

1. Go to [Railway Dashboard](https://railway.app/dashboard)
2. Select your project: **learning-platform**
3. Navigate to **Settings** → **Environment Variables**
4. Add these variables:

#### Required Variables:
```
Name: DATABASE_URL
Value: mysql://username:password@hostname:port/database_name
Environments: Production, Preview, Development
```

```
Name: GEMINI_API_KEY
Value: your-google-gemini-api-key-here
Environments: Production, Preview, Development
```

#### Optional Variables:
```
Name: VNP_TMNCODE
Value: your-vnpay-terminal-code
Environments: Production, Preview
```

```
Name: VNP_HASHSECRET
Value: your-vnpay-hash-secret
Environments: Production, Preview
```

```
Name: CUSTOM_DOMAIN
Value: learn.nguyenstudy0504.tech
Environments: Production
```

### Method 2: Vercel CLI
```bash
# Set environment variables via CLI
vercel env add DATABASE_URL production
vercel env add GEMINI_API_KEY production
```

## 🔍 **Database URL Format**

Your `DATABASE_URL` must follow this format:
```
mysql://username:password@hostname:port/database_name
```

**Examples:**
```bash
# Local development
mysql://root:password@localhost:3306/learning_platform

# Production cloud database
mysql://user:pass@mysql.example.com:3306/learning_db
```

## ✅ **Verification**

After setting environment variables:

1. **Redeploy**: `railway up`
2. **Check variables**: `railway env ls`
3. **Test deployment**: Visit your site to verify database connectivity

## 🚀 **Your Configuration is Now Ready**

The `railway.toml` file has been cleaned up and no longer references non-existent secrets. Environment variables should be set directly in the Railway dashboard.

---
**Fixed**: July 23, 2025  
**Status**: ✅ Ready for deployment
