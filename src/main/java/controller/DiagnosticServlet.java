package controller;

import java.io.IOException;
import java.io.PrintWriter;

import dao.JPAUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.HerokuConfig;

/**
 * Diagnostic servlet to help troubleshoot deployment issues
 */
@WebServlet(name = "DiagnosticServlet", urlPatterns = {"/diagnostic", "/health"})
public class DiagnosticServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Learning Platform - Diagnostic</title></head>");
        out.println("<body>");
        out.println("<h1>Learning Platform - System Diagnostic</h1>");
        
        // Environment checks
        out.println("<h2>Environment Information</h2>");
        out.println("<ul>");
        out.println("<li>Java Version: " + System.getProperty("java.version") + "</li>");
        out.println("<li>OS: " + System.getProperty("os.name") + "</li>");
        out.println("<li>Heroku Environment: " + HerokuConfig.isHeroku() + "</li>");
        out.println("<li>Context Path: " + request.getContextPath() + "</li>");
        out.println("<li>Server Info: " + getServletContext().getServerInfo() + "</li>");
        out.println("</ul>");
        
        // Database connection test
        out.println("<h2>Database Connection</h2>");
        try {
            boolean dbConnected = JPAUtil.testConnection();
            if (dbConnected) {
                out.println("<p style='color: green;'>✅ Database connection successful</p>");
            } else {
                out.println("<p style='color: red;'>❌ Database connection failed</p>");
            }
        } catch (Exception e) {
            out.println("<p style='color: red;'>❌ Database connection error: " + e.getMessage() + "</p>");
        }
        
        // Environment variables (safe ones only)
        out.println("<h2>Environment Variables</h2>");
        out.println("<ul>");
        String port = System.getenv("PORT");
        if (port != null) {
            out.println("<li>PORT: " + port + "</li>");
        }
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null) {
            out.println("<li>DATABASE_URL: [CONFIGURED]</li>");
        } else {
            out.println("<li>DATABASE_URL: [NOT SET]</li>");
        }
        out.println("</ul>");
        
        // Links to other pages
        out.println("<h2>Navigation</h2>");
        out.println("<ul>");
        out.println("<li><a href='" + request.getContextPath() + "/''>Homepage</a></li>");
        out.println("<li><a href='" + request.getContextPath() + "/home''>Home Servlet</a></li>");
        out.println("<li><a href='" + request.getContextPath() + "/login''>Login</a></li>");
        out.println("</ul>");
        
        out.println("</body>");
        out.println("</html>");
    }
}
