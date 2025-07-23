# Complete Vercel Deployment Setup Guide
## Domain: learn.nguyenstudy0504.tech

### Step 1: Install Prerequisites

#### 1.1 Install Node.js
- Download Node.js from: https://nodejs.org/
- Choose the LTS version (Long Term Support)
- Install with default settings
- Restart your terminal/PowerShell after installation

#### 1.2 Verify Installation
Open a new PowerShell window and run:
```powershell
node --version
npm --version
```

### Step 2: Install and Setup Vercel CLI

#### 2.1 Install Vercel CLI
```powershell
npm install -g vercel
```

#### 2.2 Login to Vercel
```powershell
vercel login
```
- This will open your browser
- Login with your GitHub, GitLab, or Bitbucket account
- Or create a new Vercel account

### Step 3: Configure Your Project

#### 3.1 Navigate to Project Directory
```powershell
cd "c:\Users\Dang\Documents\New Folder\Project\LearningWebsite"
```

#### 3.2 Initialize Vercel Project
```powershell
vercel
```
- Select "Link to existing project" if you already have one
- Or create a new project
- Choose the framework: "Other"
- Set build command: `mvn clean package`
- Set output directory: `target`

### Step 4: Environment Variables Setup

#### 4.1 Set Environment Variables
Go to your Vercel dashboard (https://vercel.com/dashboard):
1. Select your project
2. Go to Settings → Environment Variables
3. Add these variables:

```
DATABASE_URL = your_production_database_url
OLLAMA_BASE_URL = your_ollama_service_url (optional)
GEMINI_API_KEY = your_gemini_api_key
```

Example DATABASE_URL for PlanetScale/MySQL:
```
mysql://username:password@host:port/database_name?sslaccept=strict
```

### Step 5: Deploy to Production

#### 5.1 Deploy with Production Flag
```powershell
vercel --prod
```

### Step 6: Configure Custom Domain

#### 6.1 Add Domain to Vercel
```powershell
vercel domains add learn.nguyenstudy0504.tech
```

#### 6.2 Assign Domain to Project
```powershell
vercel domains assign learn.nguyenstudy0504.tech
```

### Step 7: DNS Configuration

#### 7.1 Access Your Domain Registrar
- Login to where you purchased nguyenstudy0504.tech
- Go to DNS settings/management

#### 7.2 Add DNS Records (Choose One Option)

**Option A: CNAME Record (Recommended)**
```
Type: CNAME
Name: learn
Target/Value: cname.vercel-dns.com
TTL: 300 (or Auto)
```

**Option B: A Records**
```
Type: A
Name: learn
Value: 76.76.19.61
TTL: 300

Type: A  
Name: learn
Value: 76.223.126.88
TTL: 300
```

### Step 8: Wait and Verify

#### 8.1 DNS Propagation
- DNS changes can take 5 minutes to 24 hours
- Use tools like https://dnschecker.org/ to check propagation

#### 8.2 SSL Certificate
- Vercel automatically generates SSL certificates
- This happens after DNS propagation
- Check your Vercel dashboard for status

#### 8.3 Test Your Site
- Visit: https://learn.nguyenstudy0504.tech
- Test all features: login, courses, payments, AI chat
- Check SSL certificate (green lock icon)

### Step 9: Troubleshooting

#### 9.1 Common Issues

**"Domain not found" Error:**
- Check DNS configuration
- Wait for propagation (up to 24 hours)
- Verify domain ownership

**SSL Certificate Issues:**
- Ensure DNS is correctly configured
- Wait for automatic certificate generation
- Check Vercel dashboard for errors

**Application Errors:**
- Check Vercel function logs
- Verify environment variables
- Test database connectivity

#### 9.2 Useful Commands

**Check deployment status:**
```powershell
vercel list
```

**View recent deployments:**
```powershell
vercel ls
```

**View domain status:**
```powershell
vercel domains ls
```

**Remove a domain (if needed):**
```powershell
vercel domains rm learn.nguyenstudy0504.tech
```

### Step 10: Ongoing Maintenance

#### 10.1 Automatic Deployments
- Connect your GitHub repository to Vercel
- Enable automatic deployments from main branch
- Each push will trigger a new deployment

#### 10.2 Monitoring
- Use Vercel Analytics for performance insights
- Set up uptime monitoring
- Monitor database connections and performance

#### 10.3 Updates
- Keep dependencies updated
- Monitor Vercel platform updates
- Review security recommendations

---

## Quick Reference Commands

```powershell
# Install Vercel CLI
npm install -g vercel

# Login
vercel login

# Deploy to production
vercel --prod

# Add domain
vercel domains add learn.nguyenstudy0504.tech

# Assign domain
vercel domains assign learn.nguyenstudy0504.tech

# Check status
vercel list
vercel domains ls
```

## Support Resources

- Vercel Documentation: https://vercel.com/docs
- Vercel Community: https://github.com/vercel/vercel/discussions
- DNS Checker: https://dnschecker.org/
- SSL Checker: https://www.sslshopper.com/ssl-checker.html
