# Learning Management System - Vercel Production Configuration

## Project Information
- **Name**: Learning Management System
- **Domain**: learn.nguyenstudy0504.tech
- **Platform**: Vercel
- **Runtime**: Java 17 with Jakarta EE

## Environment Variables
Set these in your Vercel dashboard:

```bash
DATABASE_URL=mysql://username:password@host:port/database?sslaccept=strict&serverTimezone=UTC
OLLAMA_BASE_URL=https://your-ollama-instance.com (optional)
GEMINI_API_KEY=your_gemini_api_key
CUSTOM_DOMAIN=learn.nguyenstudy0504.tech
```

## Deployment Commands
```bash
# Install Vercel CLI
npm install -g vercel

# Deploy to production
vercel --prod

# Configure custom domain
vercel domains add learn.nguyenstudy0504.tech
vercel domains assign learn.nguyenstudy0504.tech
```

## Production Features
- ✅ Custom domain with SSL
- ✅ Global CDN distribution
- ✅ Automatic deployments from Git
- ✅ Environment-based configuration
- ✅ Enhanced AI service with fallback
- ✅ Production database support
- ✅ Security headers and optimization

## Health Check
Visit: https://learn.nguyenstudy0504.tech/diagnostic
