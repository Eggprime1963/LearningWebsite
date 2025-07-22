package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

/**
 * Utility class for AI services integration
 * Provides common functionality for chatbot and recommendation features
 */
public class AIService {
    private static final Logger logger = Logger.getLogger(AIService.class.getName());
    private static final String OLLAMA_BASE_URL = "http://localhost:11434/api";
    private static final int CONNECT_TIMEOUT = 30000; // 30 seconds
    private static final int READ_TIMEOUT = 60000; // 60 seconds
    
    /**
     * Check if AI service (Ollama) is available
     */
    public static boolean isAIServiceAvailable() {
        try {
            String healthCheckUrl = OLLAMA_BASE_URL + "/tags";
            HttpURLConnection conn = (HttpURLConnection) new URL(healthCheckUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // 5 seconds for health check
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            return responseCode == 200;
            
        } catch (Exception e) {
            logger.log(Level.INFO, "AI Service not available: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Call Ollama API with specified model and prompt
     */
    public static String callOllamaAPI(String prompt, String model) throws IOException {
        return callOllamaAPI(prompt, model, false);
    }
    
    /**
     * Call Ollama API with streaming option
     */
    public static String callOllamaAPI(String prompt, String model, boolean stream) throws IOException {
        String generateUrl = OLLAMA_BASE_URL + "/generate";
        
        JSONObject payload = new JSONObject();
        payload.put("model", model);
        payload.put("prompt", prompt);
        payload.put("stream", stream);
        
        HttpURLConnection conn = (HttpURLConnection) new URL(generateUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        
        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.toString().getBytes("UTF-8"));
        }
        
        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        // Parse response
        if (stream) {
            // Handle streaming response (concatenate all response chunks)
            String[] lines = response.toString().split("\n");
            StringBuilder fullResponse = new StringBuilder();
            
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    JSONObject chunk = new JSONObject(line);
                    if (chunk.optBoolean("done", false)) break;
                    fullResponse.append(chunk.optString("response", ""));
                } catch (Exception e) {
                    // Skip malformed JSON lines
                }
            }
            
            return fullResponse.toString().trim();
        } else {
            // Handle non-streaming response
            JSONObject responseJson = new JSONObject(response.toString());
            return responseJson.optString("response", "");
        }
    }
    
    /**
     * Create educational context prompt for programming learning
     */
    public static String createEducationalPrompt(String userQuery, String context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI tutor for a programming learning platform. ");
        prompt.append("Provide helpful, clear, and educational responses. ");
        prompt.append("Focus on teaching concepts and encouraging learning.\n\n");
        
        if (context != null && !context.trim().isEmpty()) {
            prompt.append("Context: ").append(context).append("\n\n");
        }
        
        prompt.append("Student question: ").append(userQuery).append("\n\n");
        prompt.append("Provide a helpful, educational response:");
        
        return prompt.toString();
    }
    
    /**
     * Create recommendation prompt for course suggestions
     */
    public static String createRecommendationPrompt(String userPreferences, String availableCourses) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI course recommendation system for a programming education platform. ");
        prompt.append("Recommend the most suitable courses based on user preferences and learning goals.\n\n");
        
        prompt.append("User preferences: ").append(userPreferences).append("\n\n");
        prompt.append("Available courses:\n").append(availableCourses).append("\n\n");
        
        prompt.append("Provide recommendations in JSON format: ");
        prompt.append("{\"recommendations\": [\"Course Name 1\", \"Course Name 2\", ...], ");
        prompt.append("\"reasoning\": \"Brief explanation of why these courses are recommended\"}");
        
        return prompt.toString();
    }
    
    /**
     * Create programming help prompt for code assistance
     */
    public static String createProgrammingHelpPrompt(String codeQuestion, String programmingLanguage) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert programming tutor specializing in ");
        prompt.append(programmingLanguage != null ? programmingLanguage : "various programming languages");
        prompt.append(". Help students understand programming concepts, debug code, and learn best practices.\n\n");
        
        prompt.append("Programming question: ").append(codeQuestion).append("\n\n");
        prompt.append("Provide clear explanations with examples when helpful. ");
        prompt.append("Focus on teaching rather than just giving answers:");
        
        return prompt.toString();
    }
    
    /**
     * Sanitize AI response for safe display
     */
    public static String sanitizeAIResponse(String response) {
        if (response == null) return "";
        
        // Remove potentially harmful content and clean up formatting
        return response.trim()
                      .replaceAll("\\r\\n", "\n")
                      .replaceAll("\\r", "\n")
                      .replaceAll("\\n{3,}", "\n\n")
                      .replaceAll("<script[^>]*>.*?</script>", "")
                      .replaceAll("<[^>]+>", ""); // Remove HTML tags for security
    }
    
    /**
     * Get appropriate model based on query type
     */
    public static String getModelForQueryType(String queryType) {
        switch (queryType.toLowerCase()) {
            case "programming":
            case "code":
            case "debug":
                return "codellama"; // Better for code-related queries
            case "general":
            case "recommendation":
            case "chat":
            default:
                return "llama3"; // General purpose model
        }
    }
}
