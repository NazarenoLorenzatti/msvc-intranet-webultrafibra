package ar.com.ultrafibra.intranet.service;

import ar.com.ultrafibra.intranet.entities.User;
import ar.com.ultrafibra.intranet.responses.UserResponseRest;
import org.springframework.http.ResponseEntity;

public interface iUserService {

    public ResponseEntity<UserResponseRest> saveUser(User user);

    public ResponseEntity<UserResponseRest> editUser(User user);

    public ResponseEntity<UserResponseRest> deleteUser(Long id);

    public ResponseEntity<UserResponseRest> findAllUsers();

    public ResponseEntity<UserResponseRest> findUser(String username);
}
