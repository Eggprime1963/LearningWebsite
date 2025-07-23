#!/bin/bash

# Vercel Deployment Script for Learning Platform
# Domain: learn.nguyenstudy0504.tech

echo "🚀 Starting Vercel deployment for Learning Platform..."

# Check if Vercel CLI is installed
if ! command -v vercel &> /dev/null; then
    echo "❌ Vercel CLI is not installed. Installing..."
    npm install -g vercel
fi

# Login to Vercel (if not already logged in)
echo "🔐 Checking Vercel authentication..."
vercel whoami || vercel login

# Set up environment variables
echo "🔧 Setting up environment variables..."
echo "Please ensure these environment variables are set in your Vercel dashboard:"
echo "- DATABASE_URL: Your production database connection string"
echo "- OLLAMA_BASE_URL: Your Ollama service URL (optional)"
echo "- GEMINI_API_KEY: Your Google Gemini API key"

# Deploy to Vercel
echo "📦 Building and deploying to Vercel..."
vercel --prod

# Add custom domain
echo "🌐 Adding custom domain: learn.nguyenstudy0504.tech"
vercel domains add learn.nguyenstudy0504.tech

# Assign domain to project
echo "🔗 Assigning domain to project..."
vercel domains assign learn.nguyenstudy0504.tech

echo "✅ Deployment complete!"
echo ""
echo "📋 Next steps:"
echo "1. Verify your domain DNS settings point to Vercel"
echo "2. Configure your domain registrar to point to Vercel's nameservers"
echo "3. Check SSL certificate generation (may take a few minutes)"
echo ""
echo "🔧 Domain Configuration:"
echo "- Add CNAME record: learn -> cname.vercel-dns.com"
echo "- Or add A records:"
echo "  - A record: learn -> 76.76.19.61"
echo "  - A record: learn -> 76.223.126.88"
echo ""
echo "🌍 Your site will be available at: https://learn.nguyenstudy0504.tech"
