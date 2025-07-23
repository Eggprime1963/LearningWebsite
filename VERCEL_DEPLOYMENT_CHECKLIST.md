# Vercel Deployment Checklist
## Domain: learn.nguyenstudy0504.tech

### Pre-Deployment Checklist ✅

- [ ] **Node.js Installed**: Download from https://nodejs.org/ (LTS version)
- [ ] **Vercel CLI Installed**: `npm install -g vercel`
- [ ] **Vercel Account**: Create account at https://vercel.com/
- [ ] **Domain Access**: Have access to nguyenstudy0504.tech DNS settings
- [ ] **Database Ready**: Production MySQL database configured
- [ ] **Environment Variables**: Prepared for Vercel dashboard

### Deployment Steps ✅

#### Phase 1: Initial Setup
- [ ] **1.1** Install Node.js and restart terminal
- [ ] **1.2** Install Vercel CLI: `npm install -g vercel`
- [ ] **1.3** Login to Vercel: `vercel login`
- [ ] **1.4** Navigate to project directory

#### Phase 2: First Deployment
- [ ] **2.1** Initialize project: `vercel` (follow prompts)
- [ ] **2.2** Set framework to "Other"
- [ ] **2.3** Configure build settings
- [ ] **2.4** Deploy to production: `vercel --prod`

#### Phase 3: Domain Configuration
- [ ] **3.1** Add domain: `vercel domains add learn.nguyenstudy0504.tech`
- [ ] **3.2** Assign domain: `vercel domains assign learn.nguyenstudy0504.tech`
- [ ] **3.3** Configure DNS records (see options below)

#### Phase 4: DNS Setup (Choose One)
**Option A: CNAME (Recommended)**
- [ ] **4A.1** Add CNAME record: `learn` → `cname.vercel-dns.com`

**Option B: A Records**
- [ ] **4B.1** Add A record: `learn` → `76.76.19.61`
- [ ] **4B.2** Add A record: `learn` → `76.223.126.88`

#### Phase 5: Environment Variables
- [ ] **5.1** Go to Vercel dashboard → Project → Settings → Environment Variables
- [ ] **5.2** Add `DATABASE_URL` (production MySQL connection string)
- [ ] **5.3** Add `OLLAMA_BASE_URL` (optional, for AI service)
- [ ] **5.4** Add `GEMINI_API_KEY` (for AI fallback service)

#### Phase 6: Verification
- [ ] **6.1** Wait for DNS propagation (5 minutes to 24 hours)
- [ ] **6.2** Check SSL certificate generation in Vercel dashboard
- [ ] **6.3** Visit https://learn.nguyenstudy0504.tech
- [ ] **6.4** Test login functionality
- [ ] **6.5** Test course enrollment
- [ ] **6.6** Test payment system (VNPay)
- [ ] **6.7** Test AI chatbot
- [ ] **6.8** Test mobile responsiveness

### Troubleshooting Guide 🔧

#### DNS Issues
- [ ] **Check propagation**: Use https://dnschecker.org/
- [ ] **Verify records**: Ensure DNS records are correctly configured
- [ ] **Clear cache**: Clear browser DNS cache

#### SSL Certificate Issues
- [ ] **Check DNS**: Ensure DNS is properly configured first
- [ ] **Wait**: SSL generation can take 5-10 minutes after DNS propagation
- [ ] **Verify**: Check Vercel dashboard for certificate status

#### Application Issues
- [ ] **Check logs**: View function logs in Vercel dashboard
- [ ] **Verify variables**: Ensure all environment variables are set
- [ ] **Test database**: Verify database connectivity

### Post-Deployment Tasks 📋

#### Immediate
- [ ] **Set up monitoring**: Configure uptime monitoring
- [ ] **Test all features**: Complete functional testing
- [ ] **Performance check**: Test site speed and responsiveness

#### Ongoing
- [ ] **Enable auto-deploy**: Connect GitHub repository for automatic deployments
- [ ] **Monitor performance**: Use Vercel Analytics
- [ ] **Regular updates**: Keep dependencies and platform updated
- [ ] **Backup strategy**: Ensure database backup procedures

### Important URLs 🔗

- **Production Site**: https://learn.nguyenstudy0504.tech
- **Vercel Dashboard**: https://vercel.com/dashboard
- **DNS Checker**: https://dnschecker.org/
- **SSL Checker**: https://www.sslshopper.com/ssl-checker.html
- **Documentation**: See `COMPLETE_VERCEL_SETUP.md`

### Emergency Contacts 📞

- **Domain Registrar Support**: Contact for DNS issues
- **Vercel Support**: support@vercel.com
- **Database Provider**: Contact for database connectivity issues

---

**Last Updated**: July 23, 2025
**Deployment Target**: learn.nguyenstudy0504.tech
**Project**: Learning Management System
