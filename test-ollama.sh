#!/bin/bash
# Ollama Test Script

echo "🔍 Testing Ollama Implementation..."
echo "=================================="

echo "1. Testing Ollama Service..."
curl -s http://localhost:11434/api/tags | head -n 5

echo -e "\n2. Testing Simple Generation..."
curl -s -X POST http://localhost:11434/api/generate \
  -H "Content-Type: application/json" \
  -d '{"model":"llama3.2:latest","prompt":"Hello! Respond with exactly: AI is working properly","stream":false}' \
  --max-time 10 | head -n 10

echo -e "\n3. Testing CodeLlama..."
curl -s -X POST http://localhost:11434/api/generate \
  -H "Content-Type: application/json" \
  -d '{"model":"codellama:latest","prompt":"Write a simple Java hello world","stream":false}' \
  --max-time 10 | head -n 10

echo -e "\n4. Testing via Application..."
echo "Visit: http://localhost:8080/ and test the chatbot"
echo "Or visit: http://localhost:8080/diagnostic for system status"
