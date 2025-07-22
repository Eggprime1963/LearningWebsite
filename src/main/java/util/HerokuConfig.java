package util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Utility class for handling Heroku-specific configurations
 * Parses DATABASE_URL environment variable and provides database connection properties
 */
public class HerokuConfig {
    
    /**
     * Parses Heroku DATABASE_URL environment variable
     * Format: mysql://username:password@hostname:port/database
     */
    public static Properties getDatabaseProperties() {
        Properties props = new Properties();
        
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                URI dbUri = new URI(databaseUrl);
                
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String host = dbUri.getHost();
                int port = dbUri.getPort();
                String database = dbUri.getPath().substring(1); // Remove leading '/'
                
                String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                    host, port, database
                );
                
                props.setProperty("jakarta.persistence.jdbc.url", jdbcUrl);
                props.setProperty("jakarta.persistence.jdbc.user", username);
                props.setProperty("jakarta.persistence.jdbc.password", password);
                props.setProperty("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
                
                // Heroku-optimized Hibernate settings
                props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
                props.setProperty("hibernate.hbm2ddl.auto", "update");
                props.setProperty("hibernate.show_sql", "false");
                props.setProperty("hibernate.connection.autocommit", "false");
                
                // Connection pool settings optimized for Heroku
                props.setProperty("hibernate.c3p0.min_size", "2");
                props.setProperty("hibernate.c3p0.max_size", "10");
                props.setProperty("hibernate.c3p0.timeout", "1800");
                props.setProperty("hibernate.c3p0.max_statements", "50");
                props.setProperty("hibernate.c3p0.idle_test_period", "3000");
                props.setProperty("hibernate.c3p0.testConnectionOnCheckout", "true");
                props.setProperty("hibernate.c3p0.preferredTestQuery", "SELECT 1");
                
                System.out.println("✅ Heroku database configuration loaded successfully");
                System.out.println("📊 Database: " + database + " on " + host);
                
            } catch (URISyntaxException e) {
                System.err.println("❌ Error parsing DATABASE_URL: " + e.getMessage());
                throw new RuntimeException("Failed to parse Heroku DATABASE_URL", e);
            }
        } else {
            // Development/local configuration
            props.setProperty("jakarta.persistence.jdbc.url", 
                "jdbc:mysql://localhost:3306/learning_management?useSSL=false&serverTimezone=UTC");
            props.setProperty("jakarta.persistence.jdbc.user", "root");
            props.setProperty("jakarta.persistence.jdbc.password", "");
            props.setProperty("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
            
            System.out.println("🔧 Using local development database configuration");
        }
        
        return props;
    }
    
    /**
     * Gets the port for the application (Heroku sets PORT environment variable)
     */
    public static int getPort() {
        String port = System.getenv("PORT");
        if (port != null && !port.isEmpty()) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid PORT environment variable: " + port);
            }
        }
        return 8080; // Default port for local development
    }
    
    /**
     * Checks if running on Heroku platform
     */
    public static boolean isHeroku() {
        return System.getenv("DYNO") != null;
    }
    
    /**
     * Gets application URL (useful for generating absolute URLs)
     */
    public static String getAppUrl() {
        String appName = System.getenv("HEROKU_APP_NAME");
        if (appName != null && !appName.isEmpty()) {
            return "https://" + appName + ".herokuapp.com";
        }
        return "http://localhost:8080";
    }
    
    /**
     * Gets environment-specific configuration
     */
    public static String getEnvironment() {
        return isHeroku() ? "production" : "development";
    }
}
