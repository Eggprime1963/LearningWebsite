package controller;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.AccessControl;

@WebServlet(name = "AdminServlet", urlPatterns = {"/admin", "/admin/dashboard", "/administration"})
public class AdminServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(AdminServlet.class.getName());
    private final UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Admin-only access
        if (!AccessControl.validateAccess(request, response, AccessControl.AccessLevel.ADMIN)) {
            return;
        }
        
        try {
            // Load admin dashboard data
            List<User> allUsers = userDAO.getAllUsers();
            request.setAttribute("allUsers", allUsers);
            
            // User statistics
            long totalUsers = allUsers.size();
            long totalStudents = allUsers.stream().filter(u -> "student".equals(u.getRole())).count();
            long totalTeachers = allUsers.stream().filter(u -> "teacher".equals(u.getRole())).count();
            long totalAdmins = allUsers.stream().filter(u -> "admin".equals(u.getRole())).count();
            
            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("totalStudents", totalStudents);
            request.setAttribute("totalTeachers", totalTeachers);
            request.setAttribute("totalAdmins", totalAdmins);
            
            request.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.severe("Error loading admin dashboard: " + e.getMessage());
            AccessControl.sendErrorPage(request, response, "500", 
                "Server Error", "Unable to load admin dashboard. Please try again later.");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Admin-only access
        if (!AccessControl.validateAccess(request, response, AccessControl.AccessLevel.ADMIN)) {
            return;
        }
        
        String action = request.getParameter("action");
        
        try {
            switch (action != null ? action : "") {
                case "deleteUser":
                    handleDeleteUser(request, response);
                    break;
                case "updateUserRole":
                    handleUpdateUserRole(request, response);
                    break;
                default:
                    AccessControl.sendErrorPage(request, response, "400", 
                        "Bad Request", "Invalid action specified.");
            }
        } catch (Exception e) {
            logger.severe("Error processing admin action: " + e.getMessage());
            AccessControl.sendErrorPage(request, response, "500", 
                "Server Error", "Unable to process the requested action.");
        }
    }
    
    private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String userIdStr = request.getParameter("userId");
        User currentUser = AccessControl.getCurrentUser(request);
        
        if (userIdStr == null || userIdStr.isEmpty()) {
            AccessControl.sendErrorPage(request, response, "400", 
                "Bad Request", "User ID is required.");
            return;
        }
        
        try {
            int userId = Integer.parseInt(userIdStr);
            
            // Prevent admin from deleting themselves
            if (currentUser != null && currentUser.getId() == userId) {
                AccessControl.sendErrorPage(request, response, "400", 
                    "Bad Request", "You cannot delete your own account.");
                return;
            }
            
            // Delete user logic here
            userDAO.deleteUser(userId);
            
            response.sendRedirect(request.getContextPath() + "/admin?success=userDeleted");
            
        } catch (NumberFormatException e) {
            AccessControl.sendErrorPage(request, response, "400", 
                "Bad Request", "Invalid user ID format.");
        }
    }
    
    private void handleUpdateUserRole(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String userIdStr = request.getParameter("userId");
        String newRole = request.getParameter("newRole");
        
        if (userIdStr == null || userIdStr.isEmpty() || newRole == null || newRole.isEmpty()) {
            AccessControl.sendErrorPage(request, response, "400", 
                "Bad Request", "User ID and new role are required.");
            return;
        }
        
        try {
            int userId = Integer.parseInt(userIdStr);
            
            // Update user role logic here
            userDAO.updateUserRole(userId, newRole);
            
            response.sendRedirect(request.getContextPath() + "/admin?success=roleUpdated");
            
        } catch (NumberFormatException e) {
            AccessControl.sendErrorPage(request, response, "400", 
                "Bad Request", "Invalid user ID format.");
        }
    }
}
