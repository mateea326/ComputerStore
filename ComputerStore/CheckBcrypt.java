import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CheckBcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches("admin123", "$2a$10$5t8fbjb1WwzQirbhNRPqGOBMFNfLuY1Tv61sKs5jYFuvMA9etRB7K");
        System.out.println("Matches: " + matches);
    }
}
