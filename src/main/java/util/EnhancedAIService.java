package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Enhanced AI Service supporting both Gemini API (cloud) and Ollama (local)
 * Prioritizes Gemini API, falls back to Ollama if needed
 */
public class EnhancedAIService {
    private static final Logger logger = Logger.getLogger(EnhancedAIService.class.getName());
    
    // Gemini API configuration (primary)
    private static final String GEMINI_API_KEY = getGeminiApiKey();
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    
    // Ollama configuration (fallback)
    private static final String OLLAMA_BASE_URL = getOllamaUrl();
    private static final String OLLAMA_MODEL = "llama3.2:3b";
    
    // Timeouts
    private static final int CONNECT_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 30000; // 30 seconds
    
    /**
     * Get AI response with automatic fallback from Gemini to Ollama
     */
    public static String getAIResponse(String prompt) {
        return getAIResponse(prompt, "general");
    }
    
    /**
     * Get AI response with context and automatic fallback
     */
    public static String getAIResponse(String prompt, String context) {
        // Try Gemini first (primary service)
        if (isGeminiAvailable()) {
            try {
                String response = callGeminiAPI(prompt, context);
                logger.info("Using Gemini API for AI response");
                return response;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Gemini failed, falling back to Ollama: {0}", e.getMessage());
            }
        }
        
        // Fallback to Ollama
        if (isOllamaAvailable()) {
            try {
                String response = callOllamaAPI(prompt, context);
                logger.info("Using Ollama for AI response");
                return response;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Both Gemini and Ollama failed: {0}", e.getMessage());
            }
        }
        
        return "I'm currently unable to process your request. Please try again later.";
    }
    
    /**
     * Check if Ollama is available
     */
    public static boolean isOllamaAvailable() {
        if (OLLAMA_BASE_URL == null || OLLAMA_BASE_URL.isEmpty()) {
            return false;
        }
        
        try {
            String healthCheckUrl = OLLAMA_BASE_URL + "/api/tags";
            HttpURLConnection conn = (HttpURLConnection) new URL(healthCheckUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            return responseCode == 200;
            
        } catch (Exception e) {
            logger.log(Level.INFO, "Ollama not available: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if Gemini API is available
     */
    public static boolean isGeminiAvailable() {
        return GEMINI_API_KEY != null && !GEMINI_API_KEY.isEmpty() && !GEMINI_API_KEY.equals("your-gemini-api-key");
    }
    
    /**
     * Call Ollama API
     */
    private static String callOllamaAPI(String prompt, String context) throws IOException {
        String enhancedPrompt = enhancePromptWithContext(prompt, context);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", OLLAMA_MODEL);
        requestBody.put("prompt", enhancedPrompt);
        requestBody.put("stream", false);
        
        HttpURLConnection conn = (HttpURLConnection) new URL(OLLAMA_BASE_URL + "/api/generate").openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        }
        
        if (conn.getResponseCode() != 200) {
            throw new IOException("Ollama API error: " + conn.getResponseCode());
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.optString("response", "No response from Ollama");
        }
    }
    
    /**
     * Call Gemini API
     */
    private static String callGeminiAPI(String prompt, String context) throws IOException {
        if (!isGeminiAvailable()) {
            throw new IOException("Gemini API key not configured");
        }
        
        String enhancedPrompt = enhancePromptWithContext(prompt, context);
        
        // Build Gemini request
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        
        part.put("text", enhancedPrompt);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        requestBody.put("contents", contents);
        
        // Add generation config for better responses
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 1000);
        requestBody.put("generationConfig", generationConfig);
        
        String urlWithKey = GEMINI_BASE_URL + "?key=" + GEMINI_API_KEY;
        HttpURLConnection conn = (HttpURLConnection) new URL(urlWithKey).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        }
        
        if (conn.getResponseCode() != 200) {
            throw new IOException("Gemini API error: " + conn.getResponseCode());
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray candidates = jsonResponse.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject responseContent = candidate.optJSONObject("content");
                if (responseContent != null) {
                    JSONArray responseParts = responseContent.optJSONArray("parts");
                    if (responseParts != null && responseParts.length() > 0) {
                        JSONObject responsePart = responseParts.getJSONObject(0);
                        return responsePart.optString("text", "No response from Gemini");
                    }
                }
            }
            
            return "Unable to parse Gemini response";
        }
    }
    
    /**
     * Enhance prompt with context for better AI responses
     */
    private static String enhancePromptWithContext(String prompt, String context) {
        switch (context.toLowerCase()) {
            case "course":
                return "You are a helpful learning assistant for a programming course platform. " +
                       "Provide clear, educational responses about programming concepts. " +
                       "User question: " + prompt;
            
            case "recommendation":
                return "You are a course recommendation assistant. Help users find suitable programming courses " +
                       "based on their interests and skill level. Be specific and helpful. " +
                       "User request: " + prompt;
            
            case "programming":
                return "You are a programming tutor. Help explain programming concepts, debug code, " +
                       "and provide learning guidance. Be clear and educational. " +
                       "User question: " + prompt;
            
            default:
                return "You are a helpful AI assistant for a learning platform. " +
                       "Provide clear, helpful responses. " +
                       "User message: " + prompt;
        }
    }
    
    /**
     * Get service status for debugging
     */
    public static String getServiceStatus() {
        StringBuilder status = new StringBuilder();
        status.append("AI Service Status:\n");
        status.append("- Ollama Available: ").append(isOllamaAvailable()).append("\n");
        status.append("- Gemini Available: ").append(isGeminiAvailable()).append("\n");
        status.append("- Ollama URL: ").append(OLLAMA_BASE_URL != null ? OLLAMA_BASE_URL : "Not configured").append("\n");
        status.append("- Gemini Configured: ").append(isGeminiAvailable() ? "Yes" : "No");
        return status.toString();
    }
    
    /**
     * Get Ollama URL from environment or default
     */
    private static String getOllamaUrl() {
        String url = System.getenv("OLLAMA_BASE_URL");
        if (url == null || url.isEmpty()) {
            url = System.getProperty("ollama.base.url");
        }
        return url != null ? url : "http://localhost:11434";
    }
    
    /**
     * Get Gemini API key from environment
     */
    private static String getGeminiApiKey() {
        String key = System.getenv("GEMINI_API_KEY");
        if (key == null || key.isEmpty()) {
            key = System.getProperty("gemini.api.key");
        }
        // Fallback to hardcoded key if environment variable not set
        if (key == null || key.isEmpty()) {
            key = "key_here";
        }
        return key;
    }
}
