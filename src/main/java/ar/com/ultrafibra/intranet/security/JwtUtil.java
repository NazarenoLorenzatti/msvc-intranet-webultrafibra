package ar.com.ultrafibra.intranet.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import lombok.Data;

import org.springframework.stereotype.Service;


@Service
@Data
public class JwtUtil {
	private RSAPrivateKey privateKey;
	private RSAPublicKey publicKey;
        private String expiresDate;

	public JwtUtil(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}

        public String encode(String subject) {
            Date vencimiento =  new Date(System.currentTimeMillis() + 3600000); // 3600000
            this.expiresDate = vencimiento.toString();
		return JWT.create()
				.withSubject(subject)
				.withExpiresAt(vencimiento)
				.sign(Algorithm.RSA256(publicKey, privateKey) );
	}

}