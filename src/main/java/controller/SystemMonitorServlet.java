package controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.AccessControl;

@WebServlet(name = "SystemMonitorServlet", urlPatterns = {"/admin/system", "/admin/monitor"})
public class SystemMonitorServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(SystemMonitorServlet.class.getName());
    private final UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Admin-only access
        if (!AccessControl.validateAccess(request, response, AccessControl.AccessLevel.ADMIN)) {
            return;
        }
        
        try {
            // System statistics
            Map<String, Object> systemStats = new HashMap<>();
            
            // User statistics
            systemStats.put("totalUsers", userDAO.getUserCount());
            systemStats.put("totalStudents", userDAO.getUserCountByRole("student"));
            systemStats.put("totalTeachers", userDAO.getUserCountByRole("teacher"));
            systemStats.put("totalAdmins", userDAO.getUserCountByRole("admin"));
            
            // Course statistics (placeholder - implement when CourseDAO methods are available)
            systemStats.put("totalCourses", 0);
            systemStats.put("activeCourses", 0);
            
            // Activity statistics (placeholder - implement when ActivityDAO methods are available)
            systemStats.put("activeUsersToday", 0);
            systemStats.put("totalLoginsSeason", 0);
            
            // System health metrics
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            systemStats.put("maxMemoryMB", maxMemory / (1024 * 1024));
            systemStats.put("totalMemoryMB", totalMemory / (1024 * 1024));
            systemStats.put("usedMemoryMB", usedMemory / (1024 * 1024));
            systemStats.put("freeMemoryMB", freeMemory / (1024 * 1024));
            systemStats.put("memoryUsagePercentage", (usedMemory * 100) / totalMemory);
            
            // JVM information
            systemStats.put("javaVersion", System.getProperty("java.version"));
            systemStats.put("javaVendor", System.getProperty("java.vendor"));
            systemStats.put("osName", System.getProperty("os.name"));
            systemStats.put("osVersion", System.getProperty("os.version"));
            systemStats.put("availableProcessors", runtime.availableProcessors());
            
            // Server uptime (approximate)
            Object startTimeObj = getServletContext().getAttribute("startTime");
            long uptimeMillis = 0;
            if (startTimeObj instanceof Long) {
                long startTime = (Long) startTimeObj;
                uptimeMillis = System.currentTimeMillis() - startTime;
            }
            systemStats.put("uptimeHours", uptimeMillis / (1000 * 60 * 60));
            
            request.setAttribute("systemStats", systemStats);
            request.getRequestDispatcher("/WEB-INF/jsp/admin/system-monitor.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.severe("Error loading system monitor: " + e.getMessage());
            AccessControl.sendErrorPage(request, response, "500", 
                "Server Error", "Unable to load system monitoring data.");
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
                case "clearCache":
                    handleClearCache(request, response);
                    break;
                case "gc":
                    handleGarbageCollection(request, response);
                    break;
                case "exportLogs":
                    handleExportLogs(request, response);
                    break;
                default:
                    AccessControl.sendErrorPage(request, response, "400", 
                        "Bad Request", "Invalid system action specified.");
            }
        } catch (Exception e) {
            logger.severe("Error processing system action: " + e.getMessage());
            AccessControl.sendErrorPage(request, response, "500", 
                "Server Error", "Unable to process the system action.");
        }
    }
    
    private void handleClearCache(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        // Clear application cache if any
        // This is a placeholder - implement based on your caching strategy
        getServletContext().removeAttribute("courseCache");
        getServletContext().removeAttribute("userCache");
        
        response.sendRedirect(request.getContextPath() + "/admin/system?success=cacheCleared");
    }
    
    private void handleGarbageCollection(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        // Suggest garbage collection
        System.gc();
        
        response.sendRedirect(request.getContextPath() + "/admin/system?success=gcRequested");
    }
    
    private void handleExportLogs(HttpServletRequest req, HttpServletResponse response) 
            throws IOException {
        // This is a placeholder for log export functionality
        // In a real application, you would export server logs
        
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=system-logs.txt");
        
        StringBuilder logs = new StringBuilder();
        logs.append("System Logs Export\n");
        logs.append("==================\n");
        logs.append("Timestamp: ").append(new java.util.Date()).append("\n\n");
        logs.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        logs.append("OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("\n");
        
        Runtime runtime = Runtime.getRuntime();
        logs.append("Memory Usage: ").append((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)).append(" MB\n");
        logs.append("Available Memory: ").append(runtime.freeMemory() / (1024 * 1024)).append(" MB\n");
        logs.append("Total Memory: ").append(runtime.totalMemory() / (1024 * 1024)).append(" MB\n");
        logs.append("Max Memory: ").append(runtime.maxMemory() / (1024 * 1024)).append(" MB\n");
        
        logs.append("\n--- Recent Activity ---\n");
        logs.append("Note: Implement actual log reading based on your logging framework\n");
        
        response.getWriter().write(logs.toString());
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        // Store server start time for uptime calculation
        getServletContext().setAttribute("startTime", System.currentTimeMillis());
    }
}
