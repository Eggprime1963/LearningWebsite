package dao;

import java.net.URI;
import java.util.Properties;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {

    // The name must match the name defined in persistence.xml
    private static final String PERSISTENCE_UNIT_NAME = "LearningPlatformPU";
    private static EntityManagerFactory factory;

    /**
     * Creates and returns the singleton EntityManagerFactory instance.
     * Automatically configures for production environment if DATABASE_URL is present.
     * @return A singleton EntityManagerFactory instance.
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (factory == null) {
            try {
                String databaseUrl = System.getenv("DATABASE_URL");
                if (databaseUrl != null && !databaseUrl.isEmpty()) {
                    // Use production database configuration (Vercel/PlanetScale)
                    Properties prodProps = getDatabaseProperties(databaseUrl);
                    factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, prodProps);
                    System.out.println("🚀 EntityManagerFactory created with production configuration");
                } else {
                    // Use local configuration from persistence.xml
                    factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                    System.out.println("🔧 EntityManagerFactory created with local configuration");
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to create EntityManagerFactory: " + e.getMessage());
                throw new RuntimeException("Could not initialize database connection", e);
            }
        }
        return factory;
    }

    /**
     * Parses DATABASE_URL environment variable and creates database properties
     * @param databaseUrl The database URL from environment variable
     * @return Properties configured for the database connection
     */
    private static Properties getDatabaseProperties(String databaseUrl) {
        try {
            URI uri = new URI(databaseUrl);
            String[] userInfo = uri.getUserInfo().split(":");
            String username = userInfo[0];
            String password = userInfo[1];
            String host = uri.getHost();
            int port = uri.getPort();
            String database = uri.getPath().substring(1); // Remove leading '/'

            Properties props = new Properties();
            
            // Database connection properties
            props.setProperty("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
            props.setProperty("jakarta.persistence.jdbc.url", 
                String.format("jdbc:mysql://%s:%d/%s?useSSL=true&serverTimezone=UTC", host, port, database));
            props.setProperty("jakarta.persistence.jdbc.user", username);
            props.setProperty("jakarta.persistence.jdbc.password", password);
            
            // Hibernate configuration optimized for production
            props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
            props.setProperty("hibernate.hbm2ddl.auto", "update");
            props.setProperty("hibernate.show_sql", "false");
            props.setProperty("hibernate.format_sql", "false");
            
            // Connection pool settings optimized for cloud deployment
            props.setProperty("hibernate.c3p0.min_size", "5");
            props.setProperty("hibernate.c3p0.max_size", "20");
            props.setProperty("hibernate.c3p0.timeout", "300");
            props.setProperty("hibernate.c3p0.max_statements", "50");
            props.setProperty("hibernate.c3p0.idle_test_period", "3000");
            
            System.out.println("✅ Production database configuration loaded successfully");
            return props;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to parse DATABASE_URL: " + e.getMessage());
            throw new RuntimeException("Failed to parse DATABASE_URL", e);
        }
    }

    /**
     * Creates and returns a new EntityManager from the factory.
     * @return A new EntityManager instance.
     */
    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Closes the EntityManagerFactory to release all resources.
     * Should be called when the application is shutting down.
     */
    public static void shutdown() {
        if (factory != null) {
            System.out.println("🛑 Shutting down EntityManagerFactory...");
            factory.close();
            factory = null;
        }
    }
    
    /**
     * Test database connection
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (EntityManager em = getEntityManager()) {
            em.createNativeQuery("SELECT 1").getSingleResult();
            System.out.println("✅ Database connection test successful");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}