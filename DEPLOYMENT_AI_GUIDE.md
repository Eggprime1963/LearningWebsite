# Deployment Guide

## Vercel Deployment Configuration

### 1. Fix Git Submodules Warning

The warning "Failed to fetch one or more git submodules" can be resolved by:

1. **Remove any unused submodules:**
   ```bash
   git submodule deinit --all
   rm -rf .git/modules
   ```

2. **Add .gitmodules to .gitignore if not using submodules:**
   ```bash
   echo ".gitmodules" >> .gitignore
   ```

3. **Or configure Vercel to ignore submodules:**
   - Add to `vercel.json`: `"git": { "deploymentEnabled": { "main": true } }`

### 2. AI Service Configuration

#### Option A: Remote Ollama (Recommended for Production)

1. **Deploy Ollama on a VPS/Cloud Server:**
   ```bash
   # On your server
   curl https://ollama.ai/install.sh | sh
   ollama serve --host 0.0.0.0:11434
   ollama pull llama3.2:3b
   ```

2. **Set environment variable in Vercel:**
   ```
   OLLAMA_BASE_URL=https://your-server-ip:11434
   ```

#### Option B: Gemini API (Cloud Alternative)

1. **Get Gemini API Key:**
   - Visit: https://makersuite.google.com/app/apikey
   - Create new API key

2. **Set environment variable in Vercel:**
   ```
   GEMINI_API_KEY=your-gemini-api-key
   ```

### 3. Environment Variables Setup

Add these to your Vercel dashboard:

```
DATABASE_URL=your-production-database-url
OLLAMA_BASE_URL=https://your-ollama-server:11434
GEMINI_API_KEY=your-gemini-api-key
VNP_TMNCODE=CGW7KJK7
VNP_HASHSECRET=VGTLQQIUPSSO4ERSSAMGVFS5RRSGBEHT
```

### 4. Auto-Fallback System

The new `EnhancedAIService` automatically:
- Tries Ollama first (if available)
- Falls back to Gemini API
- Provides error handling for both

### 5. Testing

Test your AI service:
```
GET /chatbot?action=test
```

Response shows which services are available.

## Remote Ollama Setup Options

### Option 1: DigitalOcean/AWS VPS
```bash
# Install Ollama
curl https://ollama.ai/install.sh | sh

# Configure for external access
export OLLAMA_HOST=0.0.0.0:11434
ollama serve

# Pull your model
ollama pull llama3.2:3b
```

### Option 2: Railway/Render Deployment
Create a Dockerfile for Ollama:
```dockerfile
FROM ollama/ollama:latest
EXPOSE 11434
CMD ["ollama", "serve", "--host", "0.0.0.0:11434"]
```

### Option 3: Google Cloud Run
Deploy Ollama as a containerized service with automatic scaling.

## Gemini API Alternative

Gemini API is more reliable for production as it:
- Has no server management overhead
- Provides better uptime
- Scales automatically
- Has reasonable pricing

Cost comparison:
- Ollama: VPS costs (~$5-20/month)
- Gemini: Pay per request (~$0.002 per 1K tokens)
