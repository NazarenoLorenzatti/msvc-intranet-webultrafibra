package ar.com.ultrafibra.intranet.service;


import ar.com.ultrafibra.intranet.entities.UserPortalWeb;
import ar.com.ultrafibra.intranet.responses.UserPortalWebResponseRest;
import org.springframework.http.ResponseEntity;

public interface iUserPortalWebService {

    public ResponseEntity<UserPortalWebResponseRest> saveUser(UserPortalWeb userWeb);

    public  ResponseEntity<UserPortalWebResponseRest> findUser(UserPortalWeb userWeb);

    public ResponseEntity<UserPortalWebResponseRest> editUser(UserPortalWeb userWeb);

    public ResponseEntity<UserPortalWebResponseRest> findAll();
    
    public ResponseEntity<UserPortalWebResponseRest> deleteUser(Long id);

}
