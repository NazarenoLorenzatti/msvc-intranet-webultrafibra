package ar.com.ultrafibra.intranet.service.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@Data
public class CreatePassword {

    protected final String username = "30715652826";
    
    @Value("${app.secret}")
    private String secret;
//    private String secret = "30715652826MEGA@LINK@2023";
    
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
