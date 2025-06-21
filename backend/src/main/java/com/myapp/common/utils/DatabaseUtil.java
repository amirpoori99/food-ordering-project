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
            // Use SQLite configuration from hibernate.cfg.xml
            StandardServiceRegistryBuilder registryBuilder =
                    new StandardServiceRegistryBuilder().configure(); // hibernate.cfg.xml
    
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

