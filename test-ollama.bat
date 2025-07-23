@echo off
echo 🔍 Testing Ollama Implementation...
echo ==================================

echo 1. Testing Ollama Service...
powershell -Command "try { Invoke-RestMethod 'http://localhost:11434/api/tags' | ConvertTo-Json } catch { Write-Host 'Failed to connect to Ollama API' }"

echo.
echo 2. Testing Simple Generation...
powershell -Command "try { $body = @{ model='llama3.2:latest'; prompt='Hello! Respond with exactly: AI is working properly'; stream=$false } | ConvertTo-Json; Invoke-RestMethod -Uri 'http://localhost:11434/api/generate' -Method Post -Body $body -ContentType 'application/json' -TimeoutSec 10 | ConvertTo-Json } catch { Write-Host 'Generation test failed: ' $_.Exception.Message }"

echo.
echo 3. Testing Application Chatbot...
echo Visit: http://localhost:8080/ and test the chatbot
echo Or visit: http://localhost:8080/diagnostic for system status

echo.
echo 4. Manual Test Commands:
echo   ollama run llama3.2:latest "Hello, test response"
echo   ollama run codellama:latest "Write Java hello world"

pause
