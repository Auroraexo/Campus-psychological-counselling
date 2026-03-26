import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Hash value stored in database
        String hashedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa";
        
        // Test if password matches
        boolean matches = encoder.matches("123456", hashedPassword);
        System.out.println("Password '123456' matches: " + matches);
        
        // Generate new hash for comparison
        String newHash = encoder.encode("123456");
        System.out.println("New hash for '123456': " + newHash);
        System.out.println("New hash matches: " + encoder.matches("123456", newHash));
    }
}