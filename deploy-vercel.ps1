# Vercel Deployment Script for Learning Platform
# Domain: learn.nguyenstudy0504.tech

Write-Host "🚀 Starting Vercel deployment for Learning Platform..." -ForegroundColor Green

# Check if Vercel CLI is installed
try {
    vercel --version | Out-Null
    Write-Host "✅ Vercel CLI is already installed" -ForegroundColor Green
} catch {
    Write-Host "❌ Vercel CLI is not installed. Installing..." -ForegroundColor Yellow
    npm install -g vercel
}

# Check if user is logged in
Write-Host "🔐 Checking Vercel authentication..." -ForegroundColor Cyan
try {
    $whoami = vercel whoami 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Already logged in to Vercel" -ForegroundColor Green
    } else {
        Write-Host "🔑 Please log in to Vercel..." -ForegroundColor Yellow
        vercel login
    }
} catch {
    Write-Host "🔑 Please log in to Vercel..." -ForegroundColor Yellow
    vercel login
}

# Display environment variables info
Write-Host "🔧 Environment Variables Check..." -ForegroundColor Cyan
Write-Host "Please ensure these environment variables are set in your Vercel dashboard:" -ForegroundColor Yellow
Write-Host "- DATABASE_URL: Your production database connection string" -ForegroundColor White
Write-Host "- OLLAMA_BASE_URL: Your Ollama service URL (optional)" -ForegroundColor White
Write-Host "- GEMINI_API_KEY: Your Google Gemini API key" -ForegroundColor White
Write-Host ""

# Build and deploy
Write-Host "📦 Building and deploying to Vercel..." -ForegroundColor Cyan
vercel --prod

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Deployment successful!" -ForegroundColor Green
    
    # Add custom domain
    Write-Host "🌐 Adding custom domain: learn.nguyenstudy0504.tech" -ForegroundColor Cyan
    vercel domains add learn.nguyenstudy0504.tech 2>&1 | Out-Null
    
    # Assign domain to project
    Write-Host "🔗 Assigning domain to project..." -ForegroundColor Cyan
    vercel domains assign learn.nguyenstudy0504.tech 2>&1 | Out-Null
    
    Write-Host ""
    Write-Host "✅ Deployment complete!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 Next steps:" -ForegroundColor Yellow
    Write-Host "1. Configure your domain DNS settings" -ForegroundColor White
    Write-Host "2. Wait for SSL certificate generation (may take a few minutes)" -ForegroundColor White
    Write-Host "3. Test your application" -ForegroundColor White
    Write-Host ""
    Write-Host "🔧 Domain Configuration Options:" -ForegroundColor Yellow
    Write-Host "Option 1 - CNAME (Recommended):" -ForegroundColor White
    Write-Host "  - Add CNAME record: learn -> cname.vercel-dns.com" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Option 2 - A Records:" -ForegroundColor White
    Write-Host "  - A record: learn -> 76.76.19.61" -ForegroundColor Gray
    Write-Host "  - A record: learn -> 76.223.126.88" -ForegroundColor Gray
    Write-Host ""
    Write-Host "🌍 Your site will be available at: https://learn.nguyenstudy0504.tech" -ForegroundColor Green
} else {
    Write-Host "❌ Deployment failed. Please check the output above for errors." -ForegroundColor Red
}
