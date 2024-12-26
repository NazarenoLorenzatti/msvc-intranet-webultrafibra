package ar.com.ultrafibra.intranet.service;

import ar.com.ultrafibra.intranet.responses.JWTResponseRest;
import org.springframework.http.ResponseEntity;

public interface iLoginService {
    
    public ResponseEntity<JWTResponseRest> login(String userName, String password);
}
