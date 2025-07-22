package dao;

import java.util.Properties;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import util.HerokuConfig;

public class JPAUtil {

    // The name must match the name defined in persistence.xml
    private static final String PERSISTENCE_UNIT_NAME = "LearningPlatformPU";
    private static EntityManagerFactory factory;

    /**
     * Creates and returns the singleton EntityManagerFactory instance.
     * Automatically configures for Heroku environment if DATABASE_URL is present.
     * @return A singleton EntityManagerFactory instance.
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (factory == null) {
            try {
                if (HerokuConfig.isHeroku()) {
                    // Use Heroku database configuration
                    Properties herokuProps = HerokuConfig.getDatabaseProperties();
                    factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, herokuProps);
                    System.out.println("🚀 EntityManagerFactory created with Heroku configuration");
                } else {
                    // Use local configuration from persistence.xml
                    factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                    System.out.println("🔧 EntityManagerFactory created with local configuration");
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to create EntityManagerFactory: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Could not initialize database connection", e);
            }
        }
        return factory;
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
        try {
            EntityManager em = getEntityManager();
            em.createNativeQuery("SELECT 1").getSingleResult();
            em.close();
            System.out.println("✅ Database connection test successful");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}