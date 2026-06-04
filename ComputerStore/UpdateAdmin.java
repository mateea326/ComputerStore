import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class UpdateAdmin {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/computer_store?currentSchema=computer_store";
        String user = "postgres";
        String pass = "postgres";
        Connection conn = DriverManager.getConnection(url, user, pass);
        PreparedStatement ps = conn.prepareStatement("UPDATE users SET password = ? WHERE username = 'admin'");
        // This is the BCrypt hash for "admin123"
        ps.setString(1, "$2a$10$jD5DtQ9b8PYT9OCBMZNhO.AP5GTLFVHtj9QU33vQb7KWTBLdw3OvW");
        ps.executeUpdate();
        System.out.println("Admin password updated");
    }
}
