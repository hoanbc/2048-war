package com.example;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector(); // Initialize default connector

        // Base doc dir
        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
        ctx.addWelcomeFile("index.html");

        // Register our Servlet
        Tomcat.addServlet(ctx, "Game2048Servlet", new Game2048Servlet());
        ctx.addServletMappingDecoded("/Game2048Servlet", "Game2048Servlet");

        // Register default servlet for static resources
        Tomcat.addServlet(ctx, "default", "org.apache.catalina.servlets.DefaultServlet");
        ctx.addServletMappingDecoded("/", "default");

        // Configure static resources (Handles both IDE and JAR execution)
        File sourceFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        WebResourceRoot resources = new StandardRoot(ctx);
        if (sourceFile.isFile()) {
            // Running from JAR
            resources.addJarResources(new JarResourceSet(resources, "/", sourceFile.getAbsolutePath(), "/META-INF/resources"));
        } else {
            // Running from IDE (target/classes)
            resources.addPreResources(new DirResourceSet(resources, "/", sourceFile.getAbsolutePath() + "/META-INF/resources", "/"));
        }
        ctx.setResources(resources);

        tomcat.start();
        System.out.println("Tomcat started on port 8080");
        tomcat.getServer().await();
    }
}
