import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenHash {
    public static void main(String[] args) {
        String pwd = args.length > 0 ? args[0] : "123456";
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        System.out.println(enc.encode(pwd));
    }
}
