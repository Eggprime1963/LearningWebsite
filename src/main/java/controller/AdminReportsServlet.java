package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.AccessControl;
import dao.UserDAO;
import model.User;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet(name = "AdminReportsServlet", urlPatterns = {"/admin/reports", "/admin/analytics"})
public class AdminReportsServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(AdminReportsServlet.class.getName());
    private final UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Admin-only access
        if (!AccessControl.validateAccess(request, response, AccessControl.AccessLevel.ADMIN)) {
            return;
        }
        
        String reportType = request.getParameter("type");
        if (reportType == null) {
            reportType = "overview";
        }
        
        try {
            switch (reportType) {
                case "users":
                    generateUserReport(request, response);
                    break;
                case "activity":
                    generateActivityReport(request, response);
                    break;
                case "export":
                    exportReport(request, response);
                    break;
                default:
                    generateOverviewReport(request, response);
            }
        } catch (Exception e) {
            logger.severe("Error generating admin report: " + e.getMessage());
            AccessControl.sendErrorPage(request, response, "500", 
                "Server Error", "Unable to generate the requested report.");
        }
    }
    
    private void generateOverviewReport(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Map<String, Object> reportData = new HashMap<>();
        
        // User statistics
        long totalUsers = userDAO.getUserCount();
        long totalStudents = userDAO.getUserCountByRole("student");
        long totalTeachers = userDAO.getUserCountByRole("teacher");
        long totalAdmins = userDAO.getUserCountByRole("admin");
        
        reportData.put("totalUsers", totalUsers);
        reportData.put("totalStudents", totalStudents);
        reportData.put("totalTeachers", totalTeachers);
        reportData.put("totalAdmins", totalAdmins);
        
        // Calculate percentages
        if (totalUsers > 0) {
            reportData.put("studentPercentage", Math.round((totalStudents * 100.0) / totalUsers));
            reportData.put("teacherPercentage", Math.round((totalTeachers * 100.0) / totalUsers));
            reportData.put("adminPercentage", Math.round((totalAdmins * 100.0) / totalUsers));
        } else {
            reportData.put("studentPercentage", 0);
            reportData.put("teacherPercentage", 0);
            reportData.put("adminPercentage", 0);
        }
        
        // Recent users (last 10)
        List<User> recentUsers = userDAO.getAllUsers();
        if (recentUsers.size() > 10) {
            recentUsers = recentUsers.subList(0, 10);
        }
        reportData.put("recentUsers", recentUsers);
        
        // Generate timestamp
        reportData.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        request.setAttribute("reportData", reportData);
        request.setAttribute("reportType", "overview");
        request.getRequestDispatcher("/WEB-INF/jsp/admin/reports.jsp").forward(request, response);
    }
    
    private void generateUserReport(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Map<String, Object> reportData = new HashMap<>();
        
        // Get all users
        List<User> allUsers = userDAO.getAllUsers();
        reportData.put("allUsers", allUsers);
        
        // Users by role
        List<User> students = userDAO.getUsersByRole("student");
        List<User> teachers = userDAO.getUsersByRole("teacher");
        List<User> admins = userDAO.getUsersByRole("admin");
        
        reportData.put("students", students);
        reportData.put("teachers", teachers);
        reportData.put("admins", admins);
        
        // Statistics
        reportData.put("totalUsers", allUsers.size());
        reportData.put("totalStudents", students.size());
        reportData.put("totalTeachers", teachers.size());
        reportData.put("totalAdmins", admins.size());
        
        reportData.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        request.setAttribute("reportData", reportData);
        request.setAttribute("reportType", "users");
        request.getRequestDispatcher("/WEB-INF/jsp/admin/reports.jsp").forward(request, response);
    }
    
    private void generateActivityReport(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Map<String, Object> reportData = new HashMap<>();
        
        // Placeholder for activity data - implement when you have activity tracking
        reportData.put("dailyLogins", 0);
        reportData.put("weeklyLogins", 0);
        reportData.put("monthlyLogins", 0);
        reportData.put("activeUsers", 0);
        reportData.put("newRegistrations", 0);
        
        // Recent activity placeholder
        reportData.put("recentActivities", List.of());
        
        reportData.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        request.setAttribute("reportData", reportData);
        request.setAttribute("reportType", "activity");
        request.getRequestDispatcher("/WEB-INF/jsp/admin/reports.jsp").forward(request, response);
    }
    
    private void exportReport(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String format = request.getParameter("format");
        String type = request.getParameter("reportType");
        
        if (format == null) format = "csv";
        if (type == null) type = "users";
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=admin-report-" + type + ".csv");
        
        StringBuilder csv = new StringBuilder();
        
        switch (type) {
            case "users":
                exportUsersCSV(csv);
                break;
            case "activity":
                exportActivityCSV(csv);
                break;
            default:
                exportOverviewCSV(csv);
        }
        
        response.getWriter().write(csv.toString());
    }
    
    private void exportUsersCSV(StringBuilder csv) {
        csv.append("ID,Username,Email,Full Name,Role,Created At\n");
        
        List<User> users = userDAO.getAllUsers();
        for (User user : users) {
            csv.append(user.getId()).append(",")
               .append('"').append(user.getUsername()).append('"').append(",")
               .append('"').append(user.getEmail()).append('"').append(",")
               .append('"').append(user.getFullName() != null ? user.getFullName() : "").append('"').append(",")
               .append('"').append(user.getRole()).append('"').append(",")
               .append('"').append("N/A").append('"') // Add created_at when available
               .append("\n");
        }
    }
    
    private void exportActivityCSV(StringBuilder csv) {
        csv.append("Activity Type,Count,Period\n");
        csv.append("Daily Logins,0,Today\n");
        csv.append("Weekly Logins,0,This Week\n");
        csv.append("Monthly Logins,0,This Month\n");
        csv.append("Active Users,0,Current\n");
        csv.append("New Registrations,0,This Month\n");
    }
    
    private void exportOverviewCSV(StringBuilder csv) {
        csv.append("Metric,Count,Percentage\n");
        
        long totalUsers = userDAO.getUserCount();
        long totalStudents = userDAO.getUserCountByRole("student");
        long totalTeachers = userDAO.getUserCountByRole("teacher");
        long totalAdmins = userDAO.getUserCountByRole("admin");
        
        csv.append("Total Users,").append(totalUsers).append(",100%\n");
        
        if (totalUsers > 0) {
            csv.append("Students,").append(totalStudents).append(",")
               .append(Math.round((totalStudents * 100.0) / totalUsers)).append("%\n");
            csv.append("Teachers,").append(totalTeachers).append(",")
               .append(Math.round((totalTeachers * 100.0) / totalUsers)).append("%\n");
            csv.append("Admins,").append(totalAdmins).append(",")
               .append(Math.round((totalAdmins * 100.0) / totalUsers)).append("%\n");
        }
    }
}
