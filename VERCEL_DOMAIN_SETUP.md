# Vercel Domain Configuration Guide
## Domain: learn.nguyenstudy0504.tech

### 1. Prerequisites
- Vercel account set up
- Domain purchased and owned (nguyenstudy0504.tech)
- Access to domain registrar's DNS settings

### 2. Vercel Project Configuration
The `vercel.json` file has been configured with:
- Custom domain alias: `learn.nguyenstudy0504.tech`
- Proper routing for static assets
- Security headers
- Environment variables

### 3. Domain DNS Configuration

#### Option A: CNAME Record (Recommended)
Add this CNAME record in your domain registrar's DNS settings:

```
Type: CNAME
Name: learn
Value: cname.vercel-dns.com
TTL: 300 (or Auto)
```

#### Option B: A Records
If CNAME is not available, use these A records:

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

### 4. Deployment Steps

1. **Install Vercel CLI** (if not already installed):
   ```bash
   npm install -g vercel
   ```

2. **Login to Vercel**:
   ```bash
   vercel login
   ```

3. **Deploy with custom domain**:
   ```bash
   vercel --prod
   ```

4. **Add domain to Vercel**:
   ```bash
   vercel domains add learn.nguyenstudy0504.tech
   ```

5. **Assign domain to project**:
   ```bash
   vercel domains assign learn.nguyenstudy0504.tech
   ```

### 5. Environment Variables
Set these in your Vercel dashboard (https://vercel.com/dashboard):

- `DATABASE_URL`: Your production MySQL database connection string
- `OLLAMA_BASE_URL`: Your Ollama service URL (optional)
- `GEMINI_API_KEY`: Your Google Gemini API key for AI features

### 6. SSL Certificate
- Vercel automatically generates SSL certificates for custom domains
- This process may take 5-10 minutes after DNS propagation
- You can check the status in your Vercel dashboard

### 7. Verification
After deployment and DNS propagation (can take up to 24 hours):

1. Visit: https://learn.nguyenstudy0504.tech
2. Check SSL certificate is valid (green lock icon)
3. Test all application features
4. Verify database connectivity

### 8. Troubleshooting

#### DNS Propagation Issues
- Use online DNS checkers to verify propagation
- DNS changes can take up to 24 hours globally
- Try accessing from different networks/devices

#### SSL Certificate Issues
- Ensure DNS is properly configured
- Wait for Vercel to complete certificate generation
- Check Vercel dashboard for any error messages

#### Application Issues
- Check Vercel function logs in the dashboard
- Verify environment variables are set correctly
- Ensure database is accessible from Vercel's servers

### 9. Additional Configuration

#### Custom Error Pages
The application includes error handling, but you can add custom error pages by creating:
- `public/404.html` for 404 errors
- `public/500.html` for server errors

#### Performance Optimization
- Static assets are properly cached
- Gzip compression is enabled by default
- Consider implementing CDN for images if needed

### 10. Monitoring
- Use Vercel Analytics for performance monitoring
- Set up uptime monitoring (e.g., UptimeRobot)
- Monitor database performance and connections
