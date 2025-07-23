package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA Utility class for managing EntityManager lifecycle
 * Supports both local development and production (Vercel) deployment
 */
public class JPAUtil {
    private static EntityManagerFactory entityManagerFactory;
    
    static {
        try {
            // Try to get production database configuration first
            Map<String, String> properties = getDatabaseProperties();
            
            if (properties != null && !properties.isEmpty()) {
                // Production environment - use DATABASE_URL
                entityManagerFactory = Persistence.createEntityManagerFactory("LearningPlatformPU", properties);
                System.out.println("✅ Production database configuration loaded successfully");
            } else {
                // Local development - use persistence.xml
                entityManagerFactory = Persistence.createEntityManagerFactory("LearningPlatformPU");
                System.out.println("✅ Local database configuration loaded from persistence.xml");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to create EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }
    
    /**
     * Get database properties from environment variables (for production)
     * Parses DATABASE_URL format: mysql://username:password@hostname:port/database
     */
    private static Map<String, String> getDatabaseProperties() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            return null; // Use local configuration
        }
        
        try {
            URI uri = new URI(databaseUrl);
            String scheme = uri.getScheme();
            String userInfo = uri.getUserInfo();
            String host = uri.getHost();
            int port = uri.getPort();
            String database = uri.getPath().substring(1); // Remove leading '/'
            
            if (!"mysql".equals(scheme)) {
                throw new IllegalArgumentException("Only MySQL databases are supported");
            }
            
            String[] credentials = userInfo.split(":");
            String username = credentials[0];
            String password = credentials.length > 1 ? credentials[1] : "";
            
            Map<String, String> properties = new HashMap<>();
            
            // JDBC connection properties
            properties.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
            properties.put("jakarta.persistence.jdbc.url", 
                String.format("jdbc:mysql://%s:%d/%s?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true", 
                    host, port, database));
            properties.put("jakarta.persistence.jdbc.user", username);
            properties.put("jakarta.persistence.jdbc.password", password);
            
            // Hibernate properties optimized for production
            properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            properties.put("hibernate.hbm2ddl.auto", "update");
            properties.put("hibernate.show_sql", "false"); // Disable in production
            properties.put("hibernate.format_sql", "false");
            
            // Connection pool settings for production
            properties.put("hibernate.c3p0.min_size", "5");
            properties.put("hibernate.c3p0.max_size", "20");
            properties.put("hibernate.c3p0.timeout", "300");
            properties.put("hibernate.c3p0.max_statements", "50");
            properties.put("hibernate.c3p0.idle_test_period", "3000");
            
            return properties;
            
        } catch (URISyntaxException | IllegalArgumentException e) {
            System.err.println("❌ Failed to parse DATABASE_URL: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get EntityManager instance
     */
    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            throw new IllegalStateException("EntityManagerFactory is not initialized");
        }
        return entityManagerFactory.createEntityManager();
    }
    
    /**
     * Close EntityManagerFactory (called during application shutdown)
     */
    public static void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            System.out.println("✅ EntityManagerFactory closed successfully");
        }
    }
    
    /**
     * Check if we're running in production environment
     */
    public static boolean isProduction() {
        return System.getenv("DATABASE_URL") != null;
    }
    
    /**
     * Get connection info for diagnostics
     */
    public static String getConnectionInfo() {
        if (isProduction()) {
            String databaseUrl = System.getenv("DATABASE_URL");
            try {
                URI uri = new URI(databaseUrl);
                return String.format("Production MySQL: %s:%d/%s", 
                    uri.getHost(), uri.getPort(), uri.getPath().substring(1));
            } catch (URISyntaxException e) {
                return "Production MySQL: [URL parsing error]";
            }
        } else {
            return "Local MySQL: localhost:3306/learning_management";
        }
    }
}
