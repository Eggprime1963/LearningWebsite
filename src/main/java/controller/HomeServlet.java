package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import dao.CourseDAO;
import dao.EnrollmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Course;
import model.Enrollment;
import model.User;

/**
 * Enhanced HomeServlet with AI-powered featured courses
 * @author AI-Enhanced Learning Platform
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home", "/", "/index", "/dashboard"})
public class HomeServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(HomeServlet.class.getName());
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        logger.info("HomeServlet: Processing request for " + request.getRequestURI());
        
        try {
            // Get user context for personalization
            HttpSession session = request.getSession(false);
            User currentUser = null;
            if (session != null) {
                currentUser = (User) session.getAttribute("user");
                logger.info("HomeServlet: User session found for " + session.getAttribute("username"));
            } else {
                logger.info("HomeServlet: No user session found");
            }
            
            // Initialize empty lists to prevent null pointer exceptions
            List<Course> courses = new ArrayList<>();
            List<Course> featuredCourses = new ArrayList<>();
            
            try {
                // Fetch all courses with their teacher names and lecture counts
                List<Object[]> coursesWithTeachers = courseDAO.getCoursesWithTeacherNames();
                
                for (Object[] row : coursesWithTeachers) {
                    Course course = (Course) row[0];
                    String teacherName = (String) row[1];
                    Long lectureCount = (Long) row[2];
                    course.setTeacherName(teacherName);
                    course.setLectureCount(lectureCount);
                    
                    // Check enrollment status for this course if user is logged in
                    if (currentUser != null) {
                        boolean isEnrolled = enrollmentDAO.isUserEnrolledInCourse(currentUser.getId(), course.getIdCourse());
                        course.setEnrolled(isEnrolled);
                    }
                    
                    courses.add(course);
                }
                
                logger.info("HomeServlet: Successfully loaded " + courses.size() + " courses");
                
                // Generate AI-powered featured courses
                featuredCourses = generateAIFeaturedCourses(currentUser, courses);
                
            } catch (Exception e) {
                logger.log(Level.WARNING, "HomeServlet: Error loading courses from database, continuing with empty list", e);
                // Continue execution with empty lists - don't fail the entire page
            }
            
            // Set attributes for JSP
            request.setAttribute("courses", courses);
            request.setAttribute("featuredCourses", featuredCourses);
            request.setAttribute("aiFeatured", !featuredCourses.isEmpty());
            
            // Add AI assistant availability status
            request.setAttribute("aiAssistantAvailable", isAIAssistantAvailable());
            
            logger.info("HomeServlet: Forwarding to homePage.jsp");
            request.getRequestDispatcher("/WEB-INF/jsp/homePage.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "HomeServlet: Critical error in doGet", e);
            
            // Set minimal attributes to prevent JSP errors
            request.setAttribute("courses", new ArrayList<Course>());
            request.setAttribute("featuredCourses", new ArrayList<Course>());
            request.setAttribute("aiFeatured", false);
            request.setAttribute("aiAssistantAvailable", false);
            request.setAttribute("error", "Application is starting up. Please refresh the page in a moment.");
            
            try {
                request.getRequestDispatcher("/WEB-INF/jsp/homePage.jsp").forward(request, response);
            } catch (Exception fwdError) {
                // If JSP forwarding also fails, send a simple response
                response.setContentType("text/html");
                response.getWriter().println("<html><body>");
                response.getWriter().println("<h1>Learning Platform</h1>");
                response.getWriter().println("<p>Application is starting up. Please refresh the page in a moment.</p>");
                response.getWriter().println("<a href='/'>Refresh</a>");
                response.getWriter().println("</body></html>");
            }
        }
    }
    
    private List<Course> generateAIFeaturedCourses(User user, List<Course> allCourses) {
        try {
            // Build context for AI recommendation
            String prompt = buildFeaturedCoursesPrompt(user, allCourses);
            
            // Call AI API
            String aiResponse = callOllamaAPI(prompt);
            
            // Parse response and get featured courses
            return parseFeaturedCoursesFromAI(aiResponse, allCourses);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "AI featured courses generation failed, using fallback", e);
            return getFallbackFeaturedCourses(allCourses);
        }
    }
    
    private String buildFeaturedCoursesPrompt(User user, List<Course> allCourses) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI curator for a programming learning platform. ");
        prompt.append("Select 4 courses that should be featured on the homepage. ");
        prompt.append("Consider popularity, beginner-friendliness, and current programming trends.\n\n");
        
        if (user != null) {
            prompt.append("User context: ").append(user.getRole()).append(" user\n");
            
            try {
                List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsByStudentId(user.getId());
                if (!enrollments.isEmpty()) {
                    prompt.append("User is enrolled in ").append(enrollments.size()).append(" courses\n");
                }
            } catch (Exception e) {
                // Ignore enrollment fetch errors
            }
        }
        
        prompt.append("\nAvailable courses:\n");
        for (Course course : allCourses) {
            prompt.append("- ").append(course.getName()).append(": ").append(course.getDescription());
            if (course.getLectureCount() != null) {
                prompt.append(" (").append(course.getLectureCount()).append(" lectures)");
            }
            prompt.append("\n");
        }
        
        prompt.append("\nCriteria for selection:\n");
        prompt.append("1. Beginner-friendly courses (Python, JavaScript)\n");
        prompt.append("2. Popular industry languages (Java, C++)\n");
        prompt.append("3. Modern technologies (Go, Swift, Kotlin)\n");
        prompt.append("4. Diverse learning paths\n\n");
        prompt.append("Respond with a JSON array of exactly 4 course names: [\"Course 1\", \"Course 2\", \"Course 3\", \"Course 4\"]");
        
        return prompt.toString();
    }
    
    private List<Course> parseFeaturedCoursesFromAI(String aiResponse, List<Course> allCourses) {
        List<Course> featuredCourses = new ArrayList<>();
        
        try {
            // Extract JSON array from response
            String jsonPart = aiResponse;
            if (aiResponse.contains("[") && aiResponse.contains("]")) {
                int startIndex = aiResponse.indexOf("[");
                int endIndex = aiResponse.lastIndexOf("]") + 1;
                jsonPart = aiResponse.substring(startIndex, endIndex);
            }
            
            JSONArray courseNames = new JSONArray(jsonPart);
            
            for (int i = 0; i < courseNames.length() && featuredCourses.size() < 4; i++) {
                String courseName = courseNames.getString(i);
                
                // Find matching course
                for (Course course : allCourses) {
                    if (course.getName().equalsIgnoreCase(courseName) || 
                        course.getName().toLowerCase().contains(courseName.toLowerCase())) {
                        if (!featuredCourses.contains(course)) {
                            featuredCourses.add(course);
                            break;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse AI response for featured courses", e);
        }
        
        // If AI parsing failed or didn't return enough courses, use fallback
        if (featuredCourses.size() < 4) {
            return getFallbackFeaturedCourses(allCourses);
        }
        
        return featuredCourses;
    }
    
    private List<Course> getFallbackFeaturedCourses(List<Course> allCourses) {
        List<Course> featured = new ArrayList<>();
        
        // Priority languages for featuring
        String[] priorities = {"Python", "JavaScript", "Java", "C++"};
        
        for (String priority : priorities) {
            for (Course course : allCourses) {
                if (course.getName().toLowerCase().contains(priority.toLowerCase())) {
                    featured.add(course);
                    break;
                }
            }
            if (featured.size() >= 4) break;
        }
        
        // If still not enough, add random courses
        while (featured.size() < 4 && featured.size() < allCourses.size()) {
            for (Course course : allCourses) {
                if (!featured.contains(course)) {
                    featured.add(course);
                    break;
                }
            }
        }
        
        return featured;
    }
    
    private boolean isAIAssistantAvailable() {
        try {
            // Quick health check to Ollama API
            String ollamaUrl = "http://localhost:11434/api/tags";
            HttpURLConnection conn = (HttpURLConnection) new URL(ollamaUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000); // 3 seconds timeout
            conn.setReadTimeout(3000);
            
            int responseCode = conn.getResponseCode();
            return responseCode == 200;
            
        } catch (Exception e) {
            logger.log(Level.INFO, "AI Assistant not available: " + e.getMessage());
            return false;
        }
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
        conn.setConnectTimeout(15000); // 15 seconds
        conn.setReadTimeout(30000); // 30 seconds
        
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
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    @Override
    public String getServletInfo() {
        return "AI-Enhanced Home Servlet with Featured Courses";
    }
}
