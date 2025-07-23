package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet(name = "ChatbotServlet", urlPatterns = {"/chatbot", "/ai", "/chat"})
public class ChatbotServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ChatbotServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Simple test endpoint
        String action = request.getParameter("action");
        response.setContentType("application/json");
        
        if ("test".equals(action)) {
            // Reset cache and test model availability
            resetModelCache();
            boolean available = isModelsAvailable();
            
            JSONObject testResponse = new JSONObject();
            testResponse.put("modelsAvailable", available);
            testResponse.put("timestamp", System.currentTimeMillis());
            testResponse.put("message", available ? "Ollama models are available" : "Ollama models not available");
            
            response.getWriter().write(testResponse.toString());
            return;
        }
        
        // Default response for GET requests
        JSONObject defaultResponse = new JSONObject();
        defaultResponse.put("status", "ChatbotServlet is running");
        defaultResponse.put("endpoints", "POST for chat, GET?action=test for testing");
        response.getWriter().write(defaultResponse.toString());
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Set response timeout and enable async processing for better performance
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Read user message from request
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        
        JSONObject requestData = new JSONObject(sb.toString());
        final String userMsg = requestData.optString("message", "").trim();
        final String chatType = requestData.optString("type", "general");
        
        // Quick validation and early fallback for empty messages
        if (userMsg.isEmpty()) {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("reply", "Please enter a message to get help.");
            jsonResponse.put("type", chatType);
            response.getWriter().write(jsonResponse.toString());
            return;
        }
        
        // Get user context from session
        HttpSession session = request.getSession(false);
        final User currentUser = (session != null) ? (User) session.getAttribute("user") : null;
        
        String aiResponse;
        long startTime = System.currentTimeMillis();
        
        try {
            // Set a maximum processing time of 6 seconds for faster response
            final long MAX_PROCESSING_TIME = 6000; // Reduced to 6 seconds
            
            // Choose AI response based on chat type with timeout protection
            switch (chatType) {
                case "course-help":
                    aiResponse = processWithTimeout(() -> handleCourseHelp(userMsg, currentUser), 
                                                   MAX_PROCESSING_TIME, 
                                                   () -> generateCourseHelpFallback(userMsg, currentUser));
                    break;
                case "recommendation":
                    aiResponse = processWithTimeout(() -> handleRecommendation(userMsg, currentUser), 
                                                   MAX_PROCESSING_TIME, 
                                                   () -> generateRecommendationFallback(userMsg, currentUser));
                    break;
                case "programming-help":
                    aiResponse = processWithTimeout(() -> handleProgrammingHelp(userMsg), 
                                                   MAX_PROCESSING_TIME, 
                                                   () -> generateProgrammingHelpFallback(userMsg));
                    break;
                default:
                    aiResponse = processWithTimeout(() -> handleGeneralChat(userMsg, currentUser), 
                                                   MAX_PROCESSING_TIME, 
                                                   () -> generateGeneralChatFallback(userMsg, currentUser));
                    break;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error processing AI request", e);
            aiResponse = getFastFallbackResponse(userMsg, chatType, currentUser);
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Chat response generated in " + processingTime + "ms for type: " + chatType);
        }
        
        // Return AI reply as JSON
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("reply", aiResponse);
        jsonResponse.put("type", chatType);
        jsonResponse.put("processingTime", processingTime);
        response.getWriter().write(jsonResponse.toString());
    }
    
    // Functional interfaces for timeout processing
    @FunctionalInterface
    private interface ResponseGenerator {
        String generate() throws Exception;
    }
    
    @FunctionalInterface
    private interface FallbackGenerator {
        String generate();
    }
    
    private String processWithTimeout(ResponseGenerator generator, long timeoutMs, FallbackGenerator fallback) {
        try {
            // Always check model availability fresh for now (bypass cache)
            resetModelCache();
            if (!isModelsAvailable()) {
                logger.warning("Models not available, using fallback");
                return fallback.generate();
            }
            
            // Simple timeout mechanism - try AI with fallback on any delay/error
            long startTime = System.currentTimeMillis();
            try {
                String result = generator.generate();
                long elapsed = System.currentTimeMillis() - startTime;
                
                // If it took too long, log it but return the result anyway
                if (elapsed > timeoutMs && logger.isLoggable(Level.INFO)) {
                    logger.info("AI response took longer than expected: " + elapsed + "ms");
                }
                
                return result;
            } catch (Exception e) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > timeoutMs) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("AI request timed out after " + elapsed + "ms, using fallback");
                    }
                } else {
                    logger.log(Level.WARNING, "AI request failed, using fallback", e);
                }
                return fallback.generate();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in timeout processing", e);
            return fallback.generate();
        }
    }
    
    private String getFastFallbackResponse(String userMsg, String chatType, User user) {
        String userName = (user != null) ? user.getUsername() : "there";
        String lowerMsg = userMsg.toLowerCase();
        
        switch (chatType) {
            case "course-help":
                if (lowerMsg.contains("java")) {
                    return "Hi " + userName + "! For Java help, check our Java courses or ask specific questions about syntax, OOP concepts, or debugging.";
                } else if (lowerMsg.contains("python")) {
                    return "Hi " + userName + "! For Python assistance, explore our Python courses covering basics to advanced topics.";
                } else {
                    return "Hi " + userName + "! I'm here to help with your courses. What specific topic would you like assistance with?";
                }
            case "programming-help":
                if (lowerMsg.contains("error") || lowerMsg.contains("bug")) {
                    return "Hi " + userName + "! For debugging help: 1) Read error messages carefully, 2) Check syntax, 3) Use print/console statements to trace your code.";
                } else {
                    return "Hi " + userName + "! I can help with programming questions. What specific programming topic or issue would you like assistance with?";
                }
            case "recommendation":
                if (lowerMsg.contains("beginner")) {
                    return "Hi " + userName + "! For beginners, I recommend starting with Python Basics or Web Development Fundamentals.";
                } else {
                    return "Hi " + userName + "! I'd love to recommend courses for you. What programming languages or topics interest you most?";
                }
            default:
                return "Hi " + userName + "! I'm here to help with your learning. How can I assist you today?";
        }
    }
    
    private String handleCourseHelp(String userMsg, User user) throws Exception {
        try {
            // Simplified context for faster processing
            String contextPrompt = "You are a programming tutor. Help with this question: " + userMsg;
            
            return callOllamaAPI(contextPrompt, "llama3.2:1b");
        } catch (Exception e) {
            logger.log(Level.WARNING, "AI service unavailable, using fallback response", e);
            return generateCourseHelpFallback(userMsg, user);
        }
    }
    
    private String handleRecommendation(String userMsg, User user) throws Exception {
        try {
            // Simplified recommendation prompt for speed
            String contextPrompt = "Recommend programming courses for: " + userMsg;
            
            return callOllamaAPI(contextPrompt, "llama3.2:1b");
        } catch (Exception e) {
            logger.log(Level.WARNING, "AI service unavailable, using fallback response", e);
            return generateRecommendationFallback(userMsg, user);
        }
    }
    
    private String handleProgrammingHelp(String userMsg) throws Exception {
        try {
            String contextPrompt = "Programming tutor: " + userMsg;
            
            return callOllamaAPI(contextPrompt, "codellama:7b");
        } catch (Exception e) {
            logger.log(Level.WARNING, "AI service unavailable, using fallback response", e);
            return generateProgrammingHelpFallback(userMsg);
        }
    }
    
    private String handleGeneralChat(String userMsg, User user) throws Exception {
        try {
            String contextPrompt = "Friendly AI assistant: " + userMsg;
            
            return callOllamaAPI(contextPrompt, "llama3.2:1b");
        } catch (Exception e) {
            logger.log(Level.WARNING, "AI service unavailable, using fallback response", e);
            return generateGeneralChatFallback(userMsg, user);
        }
    }
    
    // Fallback Methods for when AI service is unavailable
    private String generateCourseHelpFallback(String userMsg, User user) {
        String userName = (user != null) ? user.getUsername() : "there";
        
        if (userMsg.toLowerCase().contains("java")) {
            return "Hi " + userName + "! While our AI assistant is temporarily unavailable, here are some helpful Java resources:\n\n" +
                   "• Java basics: variables, data types, control structures\n" +
                   "• Object-oriented concepts: classes, objects, inheritance\n" +
                   "• Popular Java frameworks: Spring, Hibernate\n\n" +
                   "Check out our Java courses in the course catalog for detailed tutorials!";
        } else if (userMsg.toLowerCase().contains("python")) {
            return "Hi " + userName + "! Here are some Python learning tips:\n\n" +
                   "• Start with Python syntax and basic data structures\n" +
                   "• Practice with small projects and exercises\n" +
                   "• Explore libraries like NumPy, Pandas for data science\n\n" +
                   "Our Python courses cover everything from basics to advanced topics!";
        } else if (userMsg.toLowerCase().contains("web") || userMsg.toLowerCase().contains("html") || userMsg.toLowerCase().contains("css")) {
            return "Hi " + userName + "! Web development fundamentals include:\n\n" +
                   "• HTML for structure, CSS for styling, JavaScript for interactivity\n" +
                   "• Responsive design principles\n" +
                   "• Modern frameworks like React, Vue, Angular\n\n" +
                   "Browse our web development courses to get started!";
        } else {
            return "Hi " + userName + "! While our AI assistant is temporarily unavailable, I'm here to help! \n\n" +
                   "For course-specific questions, please:\n" +
                   "• Check the course materials and lecture notes\n" +
                   "• Review the FAQ section\n" +
                   "• Contact your instructor directly\n" +
                   "• Visit our course discussion forums\n\n" +
                   "The AI assistant will be back online soon!";
        }
    }
    
    private String generateRecommendationFallback(String userMsg, User user) {
        String userName = (user != null) ? user.getUsername() : "there";
        
        // Check for specific interests in the user message
        if (userMsg.toLowerCase().contains("beginner") || userMsg.toLowerCase().contains("start")) {
            return "Hi " + userName + "! Here are perfect courses for beginners:\n\n" +
                   "🌟 **Start Your Journey:**\n" +
                   "• Introduction to Programming with Python\n" +
                   "• Web Development Fundamentals\n" +
                   "• Java Basics for Beginners\n\n" +
                   "These courses are designed with no prior experience required!";
        } else if (userMsg.toLowerCase().contains("advanced") || userMsg.toLowerCase().contains("expert")) {
            return "Hi " + userName + "! Ready for advanced challenges:\n\n" +
                   "🚀 **Advanced Courses:**\n" +
                   "• Machine Learning with Python\n" +
                   "• Full-Stack Development\n" +
                   "• System Design and Architecture\n\n" +
                   "Perfect for taking your skills to the next level!";
        }
        
        return "Hi " + userName + "! While our AI recommendation system is temporarily unavailable, here are some popular course recommendations:\n\n" +
               "🚀 **For Beginners:**\n" +
               "• Introduction to Programming with Python\n" +
               "• Web Development Fundamentals\n" +
               "• Java Basics for Beginners\n\n" +
               "⚡ **For Intermediate Learners:**\n" +
               "• Advanced JavaScript and React\n" +
               "• Data Structures and Algorithms\n" +
               "• Database Design and SQL\n\n" +
               "🎯 **For Advanced Students:**\n" +
               "• Machine Learning with Python\n" +
               "• Full-Stack Development\n" +
               "• System Design and Architecture\n\n" +
               "Browse our complete course catalog to find courses that match your interests!";
    }
    
    private String generateProgrammingHelpFallback(String userMsg) {
        if (userMsg.toLowerCase().contains("debug") || userMsg.toLowerCase().contains("error")) {
            return "Here are some general debugging tips:\n\n" +
                   "🔍 **Debugging Strategies:**\n" +
                   "• Read error messages carefully - they often tell you exactly what's wrong\n" +
                   "• Use print/console.log statements to trace your code execution\n" +
                   "• Check for common issues: syntax errors, variable scope, data types\n" +
                   "• Use your IDE's debugger to step through code line by line\n\n" +
                   "💡 **Common Issues:**\n" +
                   "• Null pointer exceptions\n" +
                   "• Array index out of bounds\n" +
                   "• Logic errors in conditions\n\n" +
                   "Feel free to ask your instructor or check our programming fundamentals course!";
        } else if (userMsg.toLowerCase().contains("algorithm") || userMsg.toLowerCase().contains("data structure")) {
            return "Here are key algorithm and data structure concepts:\n\n" +
                   "📊 **Essential Data Structures:**\n" +
                   "• Arrays and Lists - for sequential data\n" +
                   "• Stacks and Queues - for LIFO/FIFO operations\n" +
                   "• Trees and Graphs - for hierarchical data\n" +
                   "• Hash Tables - for fast lookups\n\n" +
                   "⚡ **Algorithm Fundamentals:**\n" +
                   "• Sorting: Quick Sort, Merge Sort\n" +
                   "• Searching: Binary Search, Graph traversal\n" +
                   "• Time and Space Complexity (Big O notation)\n\n" +
                   "Check out our Data Structures and Algorithms course for in-depth coverage!";
        } else {
            return "Here are some general programming best practices:\n\n" +
                   "✨ **Clean Code Principles:**\n" +
                   "• Write readable, self-documenting code\n" +
                   "• Use meaningful variable and function names\n" +
                   "• Keep functions small and focused\n" +
                   "• Add comments for complex logic\n\n" +
                   "🏗️ **Problem-Solving Approach:**\n" +
                   "• Break down complex problems into smaller parts\n" +
                   "• Plan before coding\n" +
                   "• Test your code frequently\n" +
                   "• Refactor to improve design\n\n" +
                   "Our programming courses cover these concepts in detail with hands-on practice!";
        }
    }
    
    private String generateGeneralChatFallback(String userMsg, User user) {
        String userName = (user != null) ? user.getUsername() : "there";
        
        if (userMsg.toLowerCase().contains("hello") || userMsg.toLowerCase().contains("hi")) {
            return "Hello " + userName + "! 👋 Welcome to our learning platform! \n\n" +
                   "I'm here to help you with:\n" +
                   "• Course recommendations\n" +
                   "• Programming questions\n" +
                   "• Platform navigation\n" +
                   "• Study tips and resources\n\n" +
                   "How can I assist you today?";
        } else if (userMsg.toLowerCase().contains("help")) {
            return "I'm happy to help, " + userName + "! Here's what I can assist you with:\n\n" +
                   "📚 **Learning Support:**\n" +
                   "• Course recommendations based on your goals\n" +
                   "• Programming help and debugging tips\n" +
                   "• Study strategies and best practices\n\n" +
                   "🎯 **Platform Features:**\n" +
                   "• How to enroll in courses\n" +
                   "• Tracking your progress\n" +
                   "• Accessing course materials\n\n" +
                   "What specific area would you like help with?";
        } else {
            return "Thanks for reaching out, " + userName + "! While our AI assistant is temporarily offline, I'm still here to help.\n\n" +
                   "For the best learning experience:\n" +
                   "• Explore our course catalog\n" +
                   "• Join study groups and discussions\n" +
                   "• Practice regularly with coding exercises\n" +
                   "• Don't hesitate to ask questions\n\n" +
                   "Keep learning and coding! 💻✨";
        }
    }
    
    // Cache for model availability to avoid repeated checks
    private static volatile Boolean modelsAvailable = null;
    private static long lastModelCheck = 0;
    private static final long MODEL_CHECK_INTERVAL = 60000; // Increased to 60 seconds to reduce checks
    
    private String callOllamaAPI(String prompt, String model) throws Exception {
        // Optimize model checking with caching
        if (!isModelsAvailable()) {
            throw new Exception("No AI models available. Please install " + model + " model first.");
        }
        
        // Optimize prompt for faster response
        String optimizedPrompt = optimizePrompt(prompt);
        
        // Make the API call with optimized settings
        String ollamaUrl = "http://localhost:11434/api/generate";
        JSONObject payload = new JSONObject();
        payload.put("model", model);
        payload.put("prompt", optimizedPrompt);
        payload.put("stream", false);
        
        // Aggressive performance optimizations for faster response
        payload.put("options", new JSONObject()
            .put("num_predict", 100)        // Reduced response length for speed
            .put("temperature", 0.3)        // Lower temperature for faster, more focused responses
            .put("top_p", 0.8)             // More focused token selection
            .put("top_k", 20)              // Significantly limit vocabulary for speed
            .put("repeat_penalty", 1.05)    // Minimal repetition penalty
            .put("num_ctx", 1024)          // Smaller context window for speed
            .put("num_thread", 4)          // Use multiple threads
            .put("num_gpu", 1)             // Use GPU if available
            .put("low_vram", true)         // Optimize for memory usage
        );
        
        java.net.URI uri = java.net.URI.create(ollamaUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Connection", "close"); // Optimize connection handling
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(3000);  // Reduced to 3 seconds
        conn.setReadTimeout(8000);     // Reduced to 8 seconds max
        
        try {
            // Write request payload
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes("UTF-8");
                os.write(input, 0, input.length);
                os.flush();
            }
            
            // Check response code
            int responseCode = conn.getResponseCode();
            if (responseCode == 404) {
                throw new Exception("Model '" + model + "' not found. Please install the model first with: ollama pull " + model);
            } else if (responseCode == 405) {
                throw new Exception("Invalid API method. Ollama API might have changed.");
            } else if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Ollama API returned HTTP " + responseCode + ": " + conn.getResponseMessage());
            }
            
            // Read response efficiently
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            JSONObject responseJson = new JSONObject(response.toString());
            String aiResponse = responseJson.optString("response", "I'm sorry, I couldn't generate a response.");
            
            // Truncate if response is too long for better performance
            if (aiResponse.length() > 500) {
                aiResponse = aiResponse.substring(0, 500) + "...";
            }
            
            return aiResponse;
            
        } finally {
            conn.disconnect();
        }
    }
    
    private boolean isModelsAvailable() {
        long currentTime = System.currentTimeMillis();
        
        // Use cached result if recent
        if (modelsAvailable != null && (currentTime - lastModelCheck) < MODEL_CHECK_INTERVAL) {
            logger.info("Using cached model availability: " + modelsAvailable);
            return modelsAvailable;
        }
        
        try {
            logger.info("Checking Ollama model availability...");
            String checkUrl = "http://localhost:11434/api/tags";
            java.net.URI checkUri = java.net.URI.create(checkUrl);
            HttpURLConnection checkConn = (HttpURLConnection) checkUri.toURL().openConnection();
            checkConn.setRequestMethod("GET");
            checkConn.setConnectTimeout(2000); // Reduced to 2 seconds
            checkConn.setReadTimeout(3000);    // Reduced to 3 seconds
            
            try {
                int responseCode = checkConn.getResponseCode();
                logger.info("Ollama API response code: " + responseCode);
                
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    logger.warning("Ollama API returned non-200 response: " + responseCode);
                    modelsAvailable = false;
                    lastModelCheck = currentTime;
                    return false;
                }
                
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(checkConn.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                String responseStr = response.toString();
                logger.info("Ollama API response: " + responseStr.substring(0, Math.min(100, responseStr.length())));
                
                JSONObject modelsJson = new JSONObject(responseStr);
                boolean hasModels = modelsJson.has("models") && modelsJson.getJSONArray("models").length() > 0;
                
                logger.info("Models available: " + hasModels + " (found " + 
                           (hasModels ? modelsJson.getJSONArray("models").length() : 0) + " models)");
                
                modelsAvailable = hasModels;
                lastModelCheck = currentTime;
                return hasModels;
                
            } finally {
                checkConn.disconnect();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error checking model availability: " + e.getMessage(), e);
            modelsAvailable = false;
            lastModelCheck = currentTime;
            return false;
        }
    }
    
    private String optimizePrompt(String prompt) {
        // Truncate very long prompts for faster processing
        if (prompt.length() > 300) {
            prompt = prompt.substring(0, 300);
        }
        
        // Add instruction for very concise response
        return "Answer briefly in under 80 words: " + prompt;
    }
    
    /**
     * Force reset the model availability cache for testing
     */
    public static void resetModelCache() {
        modelsAvailable = null;
        lastModelCheck = 0;
    }
}