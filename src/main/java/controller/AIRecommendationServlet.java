package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import dao.CourseDAO;
import dao.EnrollmentDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Course;
import model.Enrollment;
import model.User;

@WebServlet(name = "AIRecommendationServlet", urlPatterns = {"/ai-recommendations", "/featured", "/smart-suggestions"})
public class AIRecommendationServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AIRecommendationServlet.class.getName());
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get user context
        HttpSession session = request.getSession(false);
        User currentUser = null;
        if (session != null) {
            currentUser = (User) session.getAttribute("user");
        }
        
        try {
            List<Course> recommendedCourses = generateAIRecommendations(currentUser);
            
            // Set attributes for JSP
            request.setAttribute("recommendedCourses", recommendedCourses);
            request.setAttribute("aiGenerated", true);
            
            // Forward to recommendations page
            request.getRequestDispatcher("/WEB-INF/jsp/aiRecommendations.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating AI recommendations", e);
            // Fallback to popular courses
            List<Course> fallbackCourses = courseDAO.getAllCourses();
            Collections.shuffle(fallbackCourses);
            request.setAttribute("recommendedCourses", fallbackCourses.subList(0, Math.min(6, fallbackCourses.size())));
            request.setAttribute("aiGenerated", false);
            request.setAttribute("error", "AI recommendations temporarily unavailable. Showing popular courses instead.");
            
            request.getRequestDispatcher("/WEB-INF/jsp/aiRecommendations.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Handle AJAX requests for dynamic recommendations
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Read request data
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            
            JSONObject requestData = new JSONObject(sb.toString());
            String userPreferences = requestData.optString("preferences", "");
            String currentCourse = requestData.optString("currentCourse", "");
            
            HttpSession session = request.getSession(false);
            User currentUser = null;
            if (session != null) {
                currentUser = (User) session.getAttribute("user");
            }
            
            List<Course> recommendations = generatePersonalizedRecommendations(currentUser, userPreferences, currentCourse);
            
            // Convert to JSON
            JSONArray jsonArray = new JSONArray();
            for (Course course : recommendations) {
                JSONObject courseJson = new JSONObject();
                courseJson.put("id", course.getIdCourse());
                courseJson.put("name", course.getName());
                courseJson.put("description", course.getDescription());
                courseJson.put("image", course.getImage());
                courseJson.put("teacherName", course.getTeacherName());
                jsonArray.put(courseJson);
            }
            
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("recommendations", jsonArray);
            jsonResponse.put("success", true);
            
            response.getWriter().write(jsonResponse.toString());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating personalized recommendations", e);
            
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("success", false);
            errorResponse.put("error", "Unable to generate recommendations");
            
            response.getWriter().write(errorResponse.toString());
        }
    }
    
    private List<Course> generateAIRecommendations(User user) throws Exception {
        List<Course> allCourses = courseDAO.getAllCourses();
        List<Course> userEnrolledCourses = new ArrayList<>();
        
        if (user != null) {
            List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsByStudentId(user.getId());
            for (Enrollment enrollment : enrollments) {
                Course course = enrollment.getCourse();
                if (course != null) {
                    userEnrolledCourses.add(course);
                }
            }
        }
        
        // Build AI prompt for recommendations
        String prompt = buildRecommendationPrompt(user, allCourses, userEnrolledCourses);
        
        // Call AI API
        String aiResponse = callOllamaAPI(prompt);
        
        // Parse AI response and match with actual courses
        return parseRecommendationsFromAI(aiResponse, allCourses, userEnrolledCourses);
    }
    
    private List<Course> generatePersonalizedRecommendations(User user, String preferences, String currentCourse) throws Exception {
        List<Course> allCourses = courseDAO.getAllCourses();
        
        String prompt = "You are an AI course recommendation system. Based on the following information, " +
                       "recommend 3-5 programming courses that would be most beneficial:\n\n" +
                       "User preferences: " + preferences + "\n" +
                       "Current course context: " + currentCourse + "\n\n" +
                       "Available courses:\n";
        
        for (Course course : allCourses) {
            prompt += "- " + course.getName() + ": " + course.getDescription() + "\n";
        }
        
        prompt += "\nPlease respond with a JSON array of course names in order of recommendation, " +
                 "formatted as: [\"Course Name 1\", \"Course Name 2\", \"Course Name 3\"]";
        
        String aiResponse = callOllamaAPI(prompt);
        
        return parseRecommendationsFromAI(aiResponse, allCourses, new ArrayList<>());
    }
    
    private String buildRecommendationPrompt(User user, List<Course> allCourses, List<Course> enrolledCourses) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI course recommendation system for a programming learning platform. ");
        prompt.append("Recommend 6 courses that would be most beneficial for the user.\n\n");
        
        if (user != null) {
            prompt.append("User profile:\n");
            prompt.append("- Role: ").append(user.getRole()).append("\n");
            prompt.append("- Username: ").append(user.getUsername()).append("\n");
            
            if (!enrolledCourses.isEmpty()) {
                prompt.append("- Already enrolled in: ");
                for (Course course : enrolledCourses) {
                    prompt.append(course.getName()).append(", ");
                }
                prompt.append("\n");
            }
        } else {
            prompt.append("User: Anonymous visitor\n");
        }
        
        prompt.append("\nAvailable courses:\n");
        for (Course course : allCourses) {
            prompt.append("- ").append(course.getName()).append(": ").append(course.getDescription()).append("\n");
        }
        
        prompt.append("\nPlease recommend courses considering:\n");
        prompt.append("1. User's current skill level and enrolled courses\n");
        prompt.append("2. Logical learning progression\n");
        prompt.append("3. Popular programming languages and frameworks\n");
        prompt.append("4. Complementary skills\n\n");
        prompt.append("Respond with a JSON array of course names: [\"Course Name 1\", \"Course Name 2\", ...]");
        
        return prompt.toString();
    }
    
    private List<Course> parseRecommendationsFromAI(String aiResponse, List<Course> allCourses, List<Course> enrolledCourses) {
        List<Course> recommendations = new ArrayList<>();
        
        try {
            // Try to parse JSON array from AI response
            String jsonPart = aiResponse;
            if (aiResponse.contains("[") && aiResponse.contains("]")) {
                int startIndex = aiResponse.indexOf("[");
                int endIndex = aiResponse.lastIndexOf("]") + 1;
                jsonPart = aiResponse.substring(startIndex, endIndex);
            }
            
            JSONArray courseNames = new JSONArray(jsonPart);
            
            for (int i = 0; i < courseNames.length() && recommendations.size() < 6; i++) {
                String courseName = courseNames.getString(i);
                
                // Find matching course
                for (Course course : allCourses) {
                    if (course.getName().equalsIgnoreCase(courseName) || 
                        course.getName().toLowerCase().contains(courseName.toLowerCase())) {
                        
                        // Don't recommend courses user is already enrolled in
                        boolean alreadyEnrolled = enrolledCourses.stream()
                            .anyMatch(enrolled -> enrolled.getIdCourse() == course.getIdCourse());
                        
                        if (!alreadyEnrolled && !recommendations.contains(course)) {
                            recommendations.add(course);
                            break;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse AI recommendations, using fallback", e);
        }
        
        // Fallback: if AI parsing failed, use smart fallback logic
        if (recommendations.isEmpty()) {
            recommendations = getSmartFallbackRecommendations(allCourses, enrolledCourses);
        }
        
        return recommendations;
    }
    
    private List<Course> getSmartFallbackRecommendations(List<Course> allCourses, List<Course> enrolledCourses) {
        List<Course> recommendations = new ArrayList<>();
        
        // Priority order for beginners
        String[] priorityOrder = {"Python", "Java", "JavaScript", "C++", "Go", "Swift", "Kotlin", "Rust", "PHP", "Ruby"};
        
        for (String priority : priorityOrder) {
            for (Course course : allCourses) {
                if (course.getName().toLowerCase().contains(priority.toLowerCase())) {
                    boolean alreadyEnrolled = enrolledCourses.stream()
                        .anyMatch(enrolled -> enrolled.getIdCourse() == course.getIdCourse());
                    
                    if (!alreadyEnrolled && !recommendations.contains(course)) {
                        recommendations.add(course);
                        if (recommendations.size() >= 6) break;
                    }
                }
            }
            if (recommendations.size() >= 6) break;
        }
        
        return recommendations;
    }
    
    private String callOllamaAPI(String prompt) throws Exception {
        String ollamaUrl = "http://localhost:11434/api/generate";
        JSONObject payload = new JSONObject();
        payload.put("model", "llama3:latest");
        payload.put("prompt", prompt);
        payload.put("stream", false);
        
        HttpURLConnection conn = (HttpURLConnection) new URL(ollamaUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.toString().getBytes("UTF-8"));
        }
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        JSONObject responseJson = new JSONObject(response.toString());
        return responseJson.optString("response", "");
    }
}
