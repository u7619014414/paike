import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class PasswordTest {
    public static void main(String[] args) throws Exception {
        String[] passwords = {"admin123", "teacher123", "user123"};

        for (String password : passwords) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(hash);
            System.out.println(password + " -> " + encoded);
        }
    }
}
