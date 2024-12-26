package ar.com.ultrafibra.intranet.controller;

import ar.com.ultrafibra.intranet.entities.UserPortalWeb;
import ar.com.ultrafibra.intranet.responses.UserPortalWebResponseRest;
import ar.com.ultrafibra.intranet.services.impl.UserPortalWebServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/intranet/restringed/user-web")
public class UserPortalWebController {

    @Autowired
    private UserPortalWebServiceImpl portalWeb;

    @PostMapping(path = "/signup")
    private ResponseEntity<UserPortalWebResponseRest> signUp(@RequestBody UserPortalWeb userWeb) throws Exception {
        return portalWeb.saveUser(userWeb);
    }

    @GetMapping(path = "/get-users")
    public ResponseEntity<UserPortalWebResponseRest> getAll() throws Exception {
        return portalWeb.findAll();
    }

    @GetMapping(path = "/delete/{id}")
    public ResponseEntity<UserPortalWebResponseRest> deleteUser(@PathVariable("id") Long id) throws Exception {
        return portalWeb.deleteUser(id);
    }

    @PostMapping(path = "/find")
    public ResponseEntity<UserPortalWebResponseRest> findUser(@RequestBody UserPortalWeb user) throws Exception {
        return portalWeb.findUser(user);
    }

    @PutMapping(path = "/update")
    private ResponseEntity<UserPortalWebResponseRest> update(@RequestBody UserPortalWeb userWeb) throws Exception {
        return portalWeb.editUser(userWeb);
    }
}
