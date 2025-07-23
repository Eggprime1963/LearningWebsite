Write-Host "🔧 Debugging Ollama Connection Issue..." -ForegroundColor Yellow
Write-Host "============================================"

# Test 1: Check if Ollama is responding
Write-Host "`n1. Testing Ollama API directly..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -TimeoutSec 5
    Write-Host "✅ Ollama API is responding" -ForegroundColor Green
    Write-Host "   Found $($response.models.Count) models:" -ForegroundColor Green
    foreach ($model in $response.models) {
        Write-Host "   - $($model.name)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Ollama API failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Test a simple generation
Write-Host "`n2. Testing Ollama generation..." -ForegroundColor Cyan
try {
    $body = @{
        model = "llama3.2:latest"
        prompt = "Respond with exactly: OLLAMA_WORKING"
        stream = $false
    } | ConvertTo-Json

    $genResponse = Invoke-RestMethod -Uri "http://localhost:11434/api/generate" -Method Post -Body $body -ContentType "application/json" -TimeoutSec 15
    Write-Host "✅ Generation test successful!" -ForegroundColor Green
    Write-Host "   Response: $($genResponse.response)" -ForegroundColor White
} catch {
    Write-Host "❌ Generation test failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Check what's wrong with Java app
Write-Host "`n3. Checking Java application..." -ForegroundColor Cyan
try {
    $appResponse = Invoke-WebRequest -Uri "http://localhost:8080/health" -UseBasicParsing -ErrorAction SilentlyContinue
    Write-Host "✅ Application is responding" -ForegroundColor Green
} catch {
    Write-Host "❌ Application health check failed" -ForegroundColor Red
    Write-Host "   This suggests the application needs to be restarted" -ForegroundColor Yellow
}

Write-Host "`n🎯 Recommendations:" -ForegroundColor Yellow
Write-Host "1. Restart your Java application to load new servlet code"
Write-Host "2. Check application logs for any connection errors"
Write-Host "3. Verify the chatbot is using the updated servlet"

Read-Host "Press Enter to continue"
