import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa";
        
        // Try common passwords
        String[] commonPasswords = {"admin", "password", "123456", "secret", "admin123", "root", "test"};
        
        for (String password : commonPasswords) {
            if (encoder.matches(password, hashedPassword)) {
                System.out.println("Found matching password: " + password);
                return;
            }
        }
        
        System.out.println("No matching password found");
        
        // Generate hash values for new passwords for reference
        System.out.println("\nGenerate hash values for new passwords:");
        for (String password : commonPasswords) {
            System.out.println(password + " -> " + encoder.encode(password));
        }
    }
}