package com.myapp;

import com.myapp.common.utils.DatabaseUtil;
import com.myapp.common.models.User;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.Transaction;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class DatabaseChecker {
    public static void main(String[] args) {
        System.out.println("🔍 Checking database contents...");
        
        try {
            Session session = DatabaseUtil.getSessionFactory().openSession();
            
            // Check Users
            System.out.println("\n👥 USERS TABLE:");
            Query<User> userQuery = session.createQuery("FROM User", User.class);
            List<User> users = userQuery.list();
            System.out.println("Total users: " + users.size());
            for (User user : users) {
                System.out.println("  - ID: " + user.getId() + 
                                 ", Name: " + user.getFullName() + 
                                 ", Phone: " + user.getPhone() + 
                                 ", Role: " + user.getRole() + 
                                 ", Active: " + user.getIsActive());
            }
            
            // Check Restaurants
            System.out.println("\n🏪 RESTAURANTS TABLE:");
            Query<Restaurant> restaurantQuery = session.createQuery("FROM Restaurant", Restaurant.class);
            List<Restaurant> restaurants = restaurantQuery.list();
            System.out.println("Total restaurants: " + restaurants.size());
            for (Restaurant restaurant : restaurants) {
                System.out.println("  - ID: " + restaurant.getId() + 
                                 ", Name: " + restaurant.getName() + 
                                 ", Status: " + restaurant.getStatus() + 
                                 ", Owner ID: " + restaurant.getOwnerId());
            }
            
            // Check Transactions
            System.out.println("\n💰 TRANSACTIONS TABLE:");
            Query<Transaction> transactionQuery = session.createQuery("FROM Transaction", Transaction.class);
            List<Transaction> transactions = transactionQuery.list();
            System.out.println("Total transactions: " + transactions.size());
            for (Transaction transaction : transactions) {
                System.out.println("  - ID: " + transaction.getId() + 
                                 ", Amount: " + transaction.getAmount() + 
                                 ", Status: " + transaction.getStatus() + 
                                 ", Type: " + transaction.getType() + 
                                 ", User ID: " + transaction.getUserId());
            }
            
            session.close();
            
        } catch (Exception e) {
            System.err.println("❌ Error checking database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void checkDatabase() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            System.out.println("🔍 بررسی محتوای دیتابیس...");
            System.out.println("=".repeat(50));
            
            // بررسی ساختار جدول users
            checkTableStructure(session, "users");
            
        } catch (Exception e) {
            System.err.println("❌ خطا در بررسی دیتابیس: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void checkTableStructure(Session session, String tableName) {
        try {
            System.out.println("📋 بررسی ساختار جدول " + tableName + ":");
            String sql = "PRAGMA table_info(" + tableName + ")";
            List<Object[]> results = session.createNativeQuery(sql, Object[].class).list();
            
            if (results.isEmpty()) {
                System.out.println("   ❌ جدول " + tableName + " وجود ندارد!");
                return;
            }
            
            for (Object[] row : results) {
                String columnName = (String) row[1];
                String dataType = (String) row[2];
                boolean notNull = (Integer) row[3] == 1;
                String defaultValue = (String) row[4];
                boolean isPrimaryKey = (Integer) row[5] == 1;
                
                System.out.printf("   📝 %s: %s%s%s%s%n", 
                    columnName, 
                    dataType,
                    notNull ? " NOT NULL" : "",
                    isPrimaryKey ? " PRIMARY KEY" : "",
                    defaultValue != null ? " DEFAULT " + defaultValue : ""
                );
            }
            System.out.println();
        } catch (Exception e) {
            System.err.println("   ❌ خطا در بررسی ساختار جدول " + tableName + ": " + e.getMessage());
        }
    }
} 