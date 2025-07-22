package util;

import java.io.IOException;
import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

/**
 * Utility class for handling access control and privilege validation
 */
public class AccessControl {
    
    // Define role hierarchy
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_STUDENT = "student";
    
    // Define access levels
    public enum AccessLevel {
        PUBLIC,      // Everyone can access
        AUTHENTICATED, // Only logged-in users
        TEACHER,     // Teachers and above
        ADMIN        // Only admins
    }
    
    /**
     * Check if user has required access level
     */
    public static boolean hasAccess(HttpServletRequest request, AccessLevel requiredLevel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return requiredLevel == AccessLevel.PUBLIC;
        }
        
        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (user == null || role == null) {
            return requiredLevel == AccessLevel.PUBLIC;
        }
        
        switch (requiredLevel) {
            case PUBLIC:
                return true;
            case AUTHENTICATED:
                return true; // User is logged in
            case TEACHER:
                return ROLE_TEACHER.equals(role) || ROLE_ADMIN.equals(role);
            case ADMIN:
                return ROLE_ADMIN.equals(role);
            default:
                return false;
        }
    }
    
    /**
     * Check if user has specific role
     */
    public static boolean hasRole(HttpServletRequest request, String requiredRole) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        
        String userRole = (String) session.getAttribute("role");
        return requiredRole.equals(userRole);
    }
    
    /**
     * Check if user has any of the specified roles
     */
    public static boolean hasAnyRole(HttpServletRequest request, String... roles) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        
        String userRole = (String) session.getAttribute("role");
        return Arrays.asList(roles).contains(userRole);
    }
    
    /**
     * Redirect to error page with specific error details
     */
    public static void sendAccessDeniedError(HttpServletRequest request, HttpServletResponse response, 
                                           String customMessage) throws IOException {
        try {
            request.setAttribute("errorCode", "403");
            request.setAttribute("errorTitle", "Access Denied");
            request.setAttribute("errorMessage", customMessage != null ? customMessage : 
                "You don't have sufficient privileges to access this resource.");
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        } catch (Exception e) {
            // Fallback if forward fails
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
        }
    }
    
    /**
     * Redirect to login page for unauthenticated users
     */
    public static void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String originalUrl = request.getRequestURI();
        if (request.getQueryString() != null) {
            originalUrl += "?" + request.getQueryString();
        }
        
        HttpSession session = request.getSession();
        session.setAttribute("originalUrl", originalUrl);
        
        response.sendRedirect(request.getContextPath() + "/login");
    }
    
    /**
     * Send generic error page
     */
    public static void sendErrorPage(HttpServletRequest request, HttpServletResponse response,
                                   String errorCode, String errorTitle, String errorMessage) throws IOException {
        try {
            request.setAttribute("errorCode", errorCode);
            request.setAttribute("errorTitle", errorTitle);
            request.setAttribute("errorMessage", errorMessage);
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendError(Integer.parseInt(errorCode), errorTitle);
        }
    }
    
    /**
     * Validate access and handle errors automatically
     */
    public static boolean validateAccess(HttpServletRequest request, HttpServletResponse response, 
                                       AccessLevel requiredLevel) throws IOException {
        if (!hasAccess(request, requiredLevel)) {
            HttpSession session = request.getSession(false);
            
            if (session == null || session.getAttribute("user") == null) {
                // User not logged in
                redirectToLogin(request, response);
            } else {
                // User logged in but insufficient privileges
                String userRole = (String) session.getAttribute("role");
                String message = String.format(
                    "This action requires %s privileges. Your current role: %s", 
                    requiredLevel.toString().toLowerCase(), 
                    userRole
                );
                sendAccessDeniedError(request, response, message);
            }
            return false;
        }
        return true;
    }
    
    /**
     * Check if user can access resource belonging to specific user
     */
    public static boolean canAccessUserResource(HttpServletRequest request, int resourceOwnerId) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        
        User currentUser = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        
        if (currentUser == null) return false;
        
        // Admin can access everything
        if (ROLE_ADMIN.equals(role)) return true;
        
        // User can access their own resources
        return currentUser.getId() == resourceOwnerId;
    }
    
    /**
     * Get current user from session
     */
    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (User) session.getAttribute("user") : null;
    }
    
    /**
     * Get current user role from session
     */
    public static String getCurrentUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (String) session.getAttribute("role") : null;
    }
    
    /**
     * Check if current user is the owner of a resource or has admin privileges
     */
    public static boolean isOwnerOrAdmin(HttpServletRequest request, int resourceOwnerId) {
        User currentUser = getCurrentUser(request);
        String role = getCurrentUserRole(request);
        
        if (currentUser == null) return false;
        
        return ROLE_ADMIN.equals(role) || currentUser.getId() == resourceOwnerId;
    }
}
