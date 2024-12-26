package ar.com.ultrafibra.intranet.controller;

import ar.com.ultrafibra.intranet.entities.User;
import ar.com.ultrafibra.intranet.responses.JWTResponseRest;
import ar.com.ultrafibra.intranet.responses.UserResponseRest;
import ar.com.ultrafibra.intranet.services.impl.LoginServiceImpl;
import ar.com.ultrafibra.intranet.services.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {
    "http://localhost:4200",
    "http://localhost:8003",
    "https://119.8.72.246",
    "https://119.8.72.246:8002",
    "http://119.8.72.246:8003",
    "https://ultrafibra.com.ar",
    "https://ultrafibra.com.ar:8002",
    "*"})
@RequestMapping("/intranet/restringed/login")
public class UserController {

    @Autowired
    private LoginServiceImpl loginService;

    @Autowired
    private UserServiceImpl userService;

    @PostMapping(path = "/signin", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JWTResponseRest> login(@RequestBody User user) throws Exception {
        return loginService.login(user.getUsername(), user.getPassword());
    }

    @PostMapping(path = "/save-user")
    public ResponseEntity<UserResponseRest> saveUser(@RequestBody User user) throws Exception {
        return userService.saveUser(user);
    }

    @PutMapping(path = "/edit-user")
    public ResponseEntity<UserResponseRest> editUser(@RequestBody User user) throws Exception {
        return userService.saveUser(user);
    }

    @DeleteMapping(path = "/delete-user/{id}")
    public ResponseEntity<UserResponseRest> saveUser(@PathVariable Long id) throws Exception {
        return userService.deleteUser(id);
    }

    @GetMapping(path = "/find-user/{username}")
    public ResponseEntity<UserResponseRest> findUser(@PathVariable String username) throws Exception {
        return userService.findUser(username);
    }

    @GetMapping(path = "/findall-users")
    public ResponseEntity<UserResponseRest> findUser() throws Exception {
        return userService.findAllUsers();
    }
}
