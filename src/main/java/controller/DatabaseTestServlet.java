package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import util.JPAUtil;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Database Test Servlet - Verifies MySQL connection and basic functionality
 * Access via: /db-test
 */
@WebServlet("/db-test")
public class DatabaseTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Database Connection Test</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 40px; }");
        out.println(".success { color: green; }");
        out.println(".error { color: red; }");
        out.println(".info { color: blue; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>🗄️ Database Connection Test</h1>");
        
        // Test database connection
        EntityManager em = null;
        try {
            out.println("<h2>Connection Information</h2>");
            out.println("<p class='info'>Environment: " + 
                (JPAUtil.isProduction() ? "Production" : "Local Development") + "</p>");
            out.println("<p class='info'>Connection: " + JPAUtil.getConnectionInfo() + "</p>");
            
            em = JPAUtil.getEntityManager();
            out.println("<p class='success'>✅ EntityManager created successfully</p>");
            
            // Test basic query
            Query query = em.createNativeQuery("SELECT VERSION() as version");
            String version = (String) query.getSingleResult();
            out.println("<p class='success'>✅ MySQL Version: " + version + "</p>");
            
            // Test database schema
            out.println("<h2>Database Schema</h2>");
            Query tableQuery = em.createNativeQuery("SHOW TABLES");
            var tables = tableQuery.getResultList();
            
            if (tables.isEmpty()) {
                out.println("<p class='error'>⚠️ No tables found. Please import the database schema.</p>");
                out.println("<p>Run: <code>mysql -u username -p database_name < learning_management_main.sql</code></p>");
            } else {
                out.println("<p class='success'>✅ Found " + tables.size() + " tables:</p>");
                out.println("<ul>");
                for (Object table : tables) {
                    out.println("<li>" + table.toString() + "</li>");
                }
                out.println("</ul>");
            }
            
            // Test users table if exists
            try {
                Query userCountQuery = em.createNativeQuery("SELECT COUNT(*) FROM users");
                Number userCount = (Number) userCountQuery.getSingleResult();
                out.println("<p class='success'>✅ Users table: " + userCount + " records</p>");
            } catch (Exception e) {
                out.println("<p class='error'>⚠️ Users table not accessible: " + e.getMessage() + "</p>");
            }
            
            // Test courses table if exists
            try {
                Query courseCountQuery = em.createNativeQuery("SELECT COUNT(*) FROM courses");
                Number courseCount = (Number) courseCountQuery.getSingleResult();
                out.println("<p class='success'>✅ Courses table: " + courseCount + " records</p>");
            } catch (Exception e) {
                out.println("<p class='error'>⚠️ Courses table not accessible: " + e.getMessage() + "</p>");
            }
            
            out.println("<h2>🎉 Database Connection Test Completed Successfully!</h2>");
            
        } catch (Exception e) {
            out.println("<h2>❌ Database Connection Failed</h2>");
            out.println("<p class='error'>Error: " + e.getMessage() + "</p>");
            out.println("<h3>Troubleshooting:</h3>");
            out.println("<ul>");
            out.println("<li>Check if MySQL server is running</li>");
            out.println("<li>Verify database credentials in persistence.xml (local) or DATABASE_URL (production)</li>");
            out.println("<li>Ensure database 'learning_management' exists</li>");
            out.println("<li>Check firewall settings and network connectivity</li>");
            out.println("</ul>");
            
            if (JPAUtil.isProduction()) {
                out.println("<h3>Production Environment:</h3>");
                out.println("<ul>");
                out.println("<li>Verify DATABASE_URL environment variable is set in Vercel</li>");
                out.println("<li>Check if database provider allows external connections</li>");
                out.println("<li>Ensure SSL is properly configured</li>");
                out.println("</ul>");
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        
        out.println("<hr>");
        out.println("<p><a href='/'>← Back to Home</a></p>");
        out.println("</body>");
        out.println("</html>");
    }
}
