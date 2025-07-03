import java.sql.*;

public class UpdateUserRole {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:backend/food_ordering.db";
        
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "UPDATE users SET role = 'ADMIN' WHERE id = 1772402";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int affectedRows = pstmt.executeUpdate();
                System.out.println("Updated " + affectedRows + " rows");
                
                // Verify the update
                String verifySql = "SELECT id, fullName, role FROM users WHERE id = 1772402";
                try (PreparedStatement verifyStmt = conn.prepareStatement(verifySql);
                     ResultSet rs = verifyStmt.executeQuery()) {
                    
                    if (rs.next()) {
                        System.out.println("User ID: " + rs.getLong("id"));
                        System.out.println("Name: " + rs.getString("fullName"));
                        System.out.println("Role: " + rs.getString("role"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 