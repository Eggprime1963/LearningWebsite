package util;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

/**
 * Main class to start embedded Tomcat server for Railway deployment
 */
public class Main {
    
    public static void main(String[] args) throws Exception {
        
        String port = System.getenv("PORT");
        if (port == null || port.isEmpty()) {
            port = "8080";
        }
        
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.parseInt(port));
        tomcat.getConnector(); // Trigger the creation of the default connector
        
        // Create webapp directory if it doesn't exist
        String webappDirLocation = "src/main/webapp/";
        File webappDir = new File(webappDirLocation);
        if (!webappDir.exists()) {
            webappDir = new File("webapp/");
        }
        if (!webappDir.exists()) {
            webappDir = new File(".");
        }
        
        Context ctx = tomcat.addWebapp("/", webappDir.getAbsolutePath());
        System.out.println("Configuring app with basedir: " + webappDir.getAbsolutePath());
        
        // Declare an alternative location for your "WEB-INF/classes" dir
        // Tomcat will use this directory for compiled classes and will scan it for annotations
        File additionWebInfClasses = new File("target/classes");
        StandardRoot resources = new StandardRoot(ctx);
        resources.addPreResources(
            new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);
        
        // Configure environment variables for production
        System.setProperty("gemini.api.key", System.getenv("GEMINI_API_KEY"));
        System.setProperty("database.url", System.getenv("DATABASE_URL"));
        System.setProperty("database.username", System.getenv("DB_USERNAME"));
        System.setProperty("database.password", System.getenv("DB_PASSWORD"));
        
        tomcat.start();
        System.out.println("Server started on port: " + port);
        tomcat.getServer().await();
    }
}
