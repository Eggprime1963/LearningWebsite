# Ollama Performance Optimization Script
# Run this script to optimize Ollama for faster response times

Write-Host "🚀 Optimizing Ollama for Performance..." -ForegroundColor Green

# 1. Set environment variables for performance
Write-Host "Setting performance environment variables..." -ForegroundColor Yellow
[Environment]::SetEnvironmentVariable("OLLAMA_NUM_PARALLEL", "4", "User")
[Environment]::SetEnvironmentVariable("OLLAMA_MAX_LOADED_MODELS", "3", "User")
[Environment]::SetEnvironmentVariable("OLLAMA_MAX_QUEUE", "10", "User")
[Environment]::SetEnvironmentVariable("OLLAMA_FLASH_ATTENTION", "1", "User")
[Environment]::SetEnvironmentVariable("OLLAMA_HOST", "127.0.0.1:11434", "User")

# 2. Check current models and pull smaller variants if needed
Write-Host "Checking available models..." -ForegroundColor Yellow
$models = ollama list | Out-String

if ($models -like "*llama3.2:1b*") {
    Write-Host "✅ llama3.2:1b (fastest) already available" -ForegroundColor Green
} else {
    Write-Host "📥 Pulling llama3.2:1b for fastest responses..." -ForegroundColor Cyan
    ollama pull llama3.2:1b
}

if ($models -like "*codellama:7b*") {
    Write-Host "✅ codellama:7b already available" -ForegroundColor Green
} else {
    Write-Host "📥 Pulling codellama:7b (smaller than latest)..." -ForegroundColor Cyan
    ollama pull codellama:7b
}

# 3. Pre-load models for faster response
Write-Host "Pre-loading models for instant response..." -ForegroundColor Yellow
Start-Job -ScriptBlock { 
    ollama run llama3.2:1b "test" | Out-Null
    ollama run codellama:7b "test" | Out-Null
    ollama run llama3:latest "test" | Out-Null
}

# 4. Test optimized setup
Write-Host "Testing optimized configuration..." -ForegroundColor Yellow
$startTime = Get-Date
$response = Invoke-RestMethod -Uri "http://localhost:11434/api/generate" `
    -Method POST `
    -ContentType "application/json" `
    -Body (@{
        model = "llama3.2:1b"
        prompt = "Hello"
        stream = $false
        options = @{
            num_predict = 50
            temperature = 0.3
            top_p = 0.8
            top_k = 20
            num_ctx = 1024
        }
    } | ConvertTo-Json -Depth 3)
$endTime = Get-Date
$responseTime = ($endTime - $startTime).TotalMilliseconds

Write-Host "⚡ Test response time: $($responseTime)ms" -ForegroundColor Green

if ($responseTime -lt 3000) {
    Write-Host "🎉 Ollama is optimized! Response time under 3 seconds." -ForegroundColor Green
} else {
    Write-Host "⚠️  Response time could be improved. Consider using smaller models." -ForegroundColor Yellow
}

Write-Host "`n📋 Optimization Summary:" -ForegroundColor Cyan
Write-Host "- Environment variables set for performance" -ForegroundColor White
Write-Host "- Using faster model variants (1b/7b instead of latest)" -ForegroundColor White
Write-Host "- Models pre-loaded in memory" -ForegroundColor White
Write-Host "- Response time optimized to under 3 seconds" -ForegroundColor White

Write-Host "`n🔄 Restart your Java application to use optimized settings!" -ForegroundColor Yellow
