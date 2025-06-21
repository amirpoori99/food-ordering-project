package com.myapp.common.utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class DatabaseUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            StandardServiceRegistryBuilder registryBuilder =
                    new StandardServiceRegistryBuilder().configure(); // hibernate.cfg.xml
    
            /* --- Dynamic JDBC source ------------------------------------ */
            String url  = System.getenv("JDBC_URL");
            String user = System.getenv("JDBC_USER");
            String pass = System.getenv("JDBC_PASSWORD");
    
                    if (url == null || url.isBlank()) {
            //-- development → fall back to SQLite file-based (lightweight)
            registryBuilder
                    .applySetting("hibernate.connection.driver_class", "org.sqlite.JDBC")
                    .applySetting("hibernate.connection.url", "jdbc:sqlite:food_ordering.db")
                    .applySetting("hibernate.connection.username", "")
                    .applySetting("hibernate.connection.password", "")
                    .applySetting("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
            } else {
                //-- running with real DB (Docker / local)
                registryBuilder
                        .applySetting("hibernate.connection.url", url)
                        .applySetting("hibernate.connection.username", user)
                        .applySetting("hibernate.connection.password", pass);
            }
            /* ------------------------------------------------------------- */
    
            StandardServiceRegistry registry = registryBuilder.build();
            Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
            return metadata.getSessionFactoryBuilder().build();
    
        } catch (Exception e) {
            throw new ExceptionInInitializerError(
                    "Initial SessionFactory failed " + e.getMessage());
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}

