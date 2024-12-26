package ar.com.ultrafibra.intranet.service.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class CreatePassword {

    protected final String username = "*********";
    private String secret = "********************";
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    protected String createPassword() {
        return encryptToMD5(this.secret + this.format.format(new Date()));
    }

    private String encryptToMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
