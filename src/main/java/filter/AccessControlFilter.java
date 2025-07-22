package filter;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.AccessControl;

/**
 * Security filter to handle access control for protected resources
 */
@WebFilter(filterName = "AccessControlFilter", urlPatterns = {
    "/assignments/*", "/courses/*", "/lectures/*", "/gradeAssignments/*",
    "/profile/*", "/myClassroom/*", "/submit/*", "/students/*", "/studentList/*"
})
public class AccessControlFilter implements Filter {
    
    private static final Logger logger = Logger.getLogger(AccessControlFilter.class.getName());
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AccessControlFilter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());
        
        logger.info("AccessControlFilter processing: " + path);
        
        // Determine required access level based on the path
        AccessControl.AccessLevel requiredLevel = determineRequiredAccessLevel(path);
        
        // Validate access
        if (!AccessControl.validateAccess(httpRequest, httpResponse, requiredLevel)) {
            return; // Access denied, response already handled by validateAccess
        }
        
        // Additional specific checks
        if (!performSpecificAccessChecks(httpRequest, httpResponse, path)) {
            return; // Access denied by specific checks
        }
        
        // Continue with the request
        chain.doFilter(request, response);
    }
    
    /**
     * Determine the required access level based on the request path
     */
    private AccessControl.AccessLevel determineRequiredAccessLevel(String path) {
        // Admin only paths
        if (path.startsWith("/admin/") || path.contains("admin")) {
            return AccessControl.AccessLevel.ADMIN;
        }
        
        // Teacher only paths
        if (path.startsWith("/assignments") || path.startsWith("/gradeAssignments") || 
            path.startsWith("/grade") || path.contains("teacher")) {
            return AccessControl.AccessLevel.TEACHER;
        }
        
        // Student and above paths
        if (path.startsWith("/courses") || path.startsWith("/lectures") || 
            path.startsWith("/submit") || path.startsWith("/myClassroom")) {
            return AccessControl.AccessLevel.AUTHENTICATED;
        }
        
        // Default to authenticated for protected resources
        return AccessControl.AccessLevel.AUTHENTICATED;
    }
    
    /**
     * Perform specific access checks based on the path and user context
     */
    private boolean performSpecificAccessChecks(HttpServletRequest request, 
                                               HttpServletResponse response, String path) throws IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        
        User currentUser = (User) session.getAttribute("user");
        String userRole = (String) session.getAttribute("role");
        
        if (currentUser == null || userRole == null) return false;
        
        // Check course ownership for teachers
        if (path.startsWith("/courses") && "teacher".equals(userRole)) {
            String courseId = request.getParameter("courseId");
            if (courseId != null && !courseId.isEmpty()) {
                // You can add logic here to check if teacher owns the course
                // For now, we'll allow all teachers to access courses
                return true;
            }
        }
        
        // Check student access to their own submissions
        if (path.startsWith("/submit") || path.contains("submission")) {
            String studentId = request.getParameter("studentId");
            if (studentId != null && !studentId.isEmpty()) {
                try {
                    int requestedStudentId = Integer.parseInt(studentId);
                    if (!AccessControl.canAccessUserResource(request, requestedStudentId)) {
                        AccessControl.sendAccessDeniedError(request, response, 
                            "You can only access your own submissions.");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    AccessControl.sendErrorPage(request, response, "400", 
                        "Bad Request", "Invalid student ID format.");
                    return false;
                }
            }
        }
        
        // Check profile access
        if (path.startsWith("/profile")) {
            String userId = request.getParameter("userId");
            if (userId != null && !userId.isEmpty()) {
                try {
                    int requestedUserId = Integer.parseInt(userId);
                    if (!AccessControl.isOwnerOrAdmin(request, requestedUserId)) {
                        AccessControl.sendAccessDeniedError(request, response, 
                            "You can only access your own profile.");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    AccessControl.sendErrorPage(request, response, "400", 
                        "Bad Request", "Invalid user ID format.");
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @Override
    public void destroy() {
        logger.info("AccessControlFilter destroyed");
    }
}
