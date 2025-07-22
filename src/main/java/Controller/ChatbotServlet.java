package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import dao.CourseDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Course;
import model.User;

@WebServlet(name = "ChatbotServlet", urlPatterns = {"/chatbot", "/ai", "/chat"})
public class ChatbotServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ChatbotServlet.class.getName());
    private final CourseDAO courseDAO = new CourseDAO();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Read user message from request
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        
        JSONObject requestData = new JSONObject(sb.toString());
        String userMsg = requestData.optString("message", "");
        String chatType = requestData.optString("type", "general"); // general, course-help, recommendation
        
        // Get user context from session
        HttpSession session = request.getSession(false);
        User currentUser = null;
        if (session != null) {
            currentUser = (User) session.getAttribute("user");
        }
        
        String aiResponse;
        try {
            // Choose AI response based on chat type and context
            switch (chatType) {
                case "course-help":
                    aiResponse = handleCourseHelp(userMsg, currentUser);
                    break;
                case "recommendation":
                    aiResponse = handleRecommendation(userMsg, currentUser);
                    break;
                case "programming-help":
                    aiResponse = handleProgrammingHelp(userMsg);
                    break;
                default:
                    aiResponse = handleGeneralChat(userMsg, currentUser);
                    break;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing AI request", e);
            aiResponse = "I'm sorry, I'm having trouble processing your request right now. Please try again later.";
        }
        
        // Return AI reply as JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("reply", aiResponse);
        jsonResponse.put("type", chatType);
        response.getWriter().write(jsonResponse.toString());
    }
    
    private String handleCourseHelp(String userMsg, User user) throws Exception {
        try {
            // Enhanced context for course-specific help
            String contextPrompt = buildCourseContextPrompt(user);
            String fullPrompt = contextPrompt + "\n\nUser question: " + userMsg + 
                               "\n\nPlease provide helpful, educational guidance related to programming and learning.";
            
            return callOllamaAPI(fullPrompt, "llama3");
        } catch (Exception e) {
            logger.log(Level.WARNING, "AI service unavailable, using fallback response", e);
            return generateCourseHelpFallback(userMsg, user);
        }
    }
    
    private String handleRecommendation(String userMsg, User user) throws Exception {
        try {
            // Get available courses for context
            List<Course> courses = courseDAO.getAllCourses();
            StringBuilder courseContext = new StringBuilder();
            courseContext.append("Available courses in our learning platform:\n");
            
            for (Course course : courses) {
                courseContext.append("- ").append(course.getName())
                            .append(": ").append(course.getDescription()).append("\n");
            }
            
            String contextPrompt = "You are an AI learning advisor for a programming education platform. " +
                                  "Help students choose the best courses based on their interests and goals.\n\n" +
                                  courseContext.toString() + 
                                  "\n\nUser request: " + userMsg +
                                  "\n\nProvide personalized course recommendations with explanations.";
            
            return callOllamaAPI(contextPrompt, "llama3");
        } catch (Exception e) {
            logger.log(Level.WARNING, "AI service unavailable, using fallback response", e);
            return generateRecommendationFallback(userMsg, user);
        }
    }
    
    private String handleProgrammingHelp(String userMsg) throws Exception {
        try {
            String contextPrompt = "You are an expert programming tutor. Help students understand programming concepts, " +
                                  "debug code, and learn best practices. Provide clear, educational explanations.\n\n" +
                                  "Student question: " + userMsg +
                                  "\n\nProvide helpful programming guidance with examples when appropriate.";
            
            return callOllamaAPI(contextPrompt, "codellama");
        } catch (Exception e) {
            logger.log(Level.WARNING, "AI service unavailable, using fallback response", e);
            return generateProgrammingHelpFallback(userMsg);
        }
    }
    
    private String handleGeneralChat(String userMsg, User user) throws Exception {
        try {
            String contextPrompt = "You are a friendly AI assistant for a programming learning platform. " +
                                  "Help students with general questions about learning, programming, and using the platform.\n\n" +
                                  "User message: " + userMsg +
                                  "\n\nProvide a helpful and encouraging response.";
            
            return callOllamaAPI(contextPrompt, "llama3");
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
    
    private String buildCourseContextPrompt(User user) {
        StringBuilder context = new StringBuilder();
        context.append("You are an AI tutor for a programming learning platform. ");
        
        if (user != null) {
            context.append("The user is ").append(user.getRole()).append(" named ").append(user.getUsername()).append(". ");
            
            // Add user-specific context based on role
            if ("student".equals(user.getRole())) {
                context.append("Help them learn programming concepts and understand course materials. ");
            } else if ("teacher".equals(user.getRole())) {
                context.append("Assist them with teaching methods and course content creation. ");
            }
        }
        
        return context.toString();
    }
    
    private String callOllamaAPI(String prompt, String model) throws Exception {
        // First check if Ollama service is available and has models
        String checkUrl = "http://localhost:11434/api/tags";
        java.net.URI checkUri = java.net.URI.create(checkUrl);
        HttpURLConnection checkConn = (HttpURLConnection) checkUri.toURL().openConnection();
        checkConn.setRequestMethod("GET");
        checkConn.setConnectTimeout(5000); // 5 seconds
        checkConn.setReadTimeout(10000); // 10 seconds
        
        try {
            int checkResponseCode = checkConn.getResponseCode();
            if (checkResponseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Ollama service not available (HTTP " + checkResponseCode + ")");
            }
            
            // Read models list
            StringBuilder modelsResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(checkConn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    modelsResponse.append(line);
                }
            }
            
            JSONObject modelsJson = new JSONObject(modelsResponse.toString());
            if (!modelsJson.has("models") || modelsJson.getJSONArray("models").length() == 0) {
                throw new Exception("No AI models installed in Ollama. Please install " + model + " model first.");
            }
        } finally {
            checkConn.disconnect();
        }
        
        // Now make the actual API call
        String ollamaUrl = "http://localhost:11434/api/generate";
        JSONObject payload = new JSONObject();
        payload.put("model", model);
        payload.put("prompt", prompt);
        payload.put("stream", false); // Get complete response at once
        
        java.net.URI uri = java.net.URI.create(ollamaUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "LearningPlatform/1.0");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(30000); // 30 seconds
        conn.setReadTimeout(60000); // 60 seconds
        
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
            
            // Read response
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            JSONObject responseJson = new JSONObject(response.toString());
            return responseJson.optString("response", "I'm sorry, I couldn't generate a response.");
            
        } finally {
            conn.disconnect();
        }
    }
}