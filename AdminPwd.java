import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AdminPwd {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String hash = encoder.encode("admin123");
        System.out.println(hash);
    }
}

