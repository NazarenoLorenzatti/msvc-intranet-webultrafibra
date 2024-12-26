package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.iUserDao;
import ar.com.ultrafibra.intranet.entities.Jwt;
import ar.com.ultrafibra.intranet.responses.JWTResponseRest;
import ar.com.ultrafibra.intranet.security.JwtUtil;
import ar.com.ultrafibra.intranet.security.UserDetailsServiceImp;
import ar.com.ultrafibra.intranet.service.iLoginService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Data
@Service
public class LoginServiceImpl implements iLoginService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsServiceImp userDetails;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private iUserDao userDao;

    @Override
    public ResponseEntity<JWTResponseRest> login(String userName, String password) {
        JWTResponseRest respuesta = new JWTResponseRest();
        List<Jwt> listaJWT = new ArrayList<>();
        try {
            UserDetails user = this.userDetails.loadUserByUsername(userName);
            if (user != null) {
                if (this.passwordEncoder.matches(password, user.getPassword())) {
                    listaJWT.add(new Jwt(this.jwtUtil.encode(user.getUsername()), this.jwtUtil.getExpiresDate()));
                    respuesta.getJwtResponse().setJwt(listaJWT);
                    // Obtener los roles del usuario
                    List<String> roles = user.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority) // Obtener el nombre del rol
                            .collect(Collectors.toList());
                    respuesta.setRoles(roles);
                    respuesta.setMetadata("Respuesta ok", "00", "Login correcto");
                    return new ResponseEntity<>(respuesta, HttpStatus.OK);

                } else {
                    respuesta.setMetadata("Respuesta nok", "-1", "credenciales incorrectas");
                    return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
                }
            } else {
                respuesta.setMetadata("Respuesta nok", "-1", "No se encontro el Usuario");
                return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
            }

        } catch (UsernameNotFoundException e) {
            respuesta.setMetadata("Respuesta nok", "-1", "Error al itentar en el servidor al intentar acceder");
            e.getStackTrace();
            return new ResponseEntity<>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
