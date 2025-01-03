package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.iUserDao;
import ar.com.ultrafibra.intranet.entities.Jwt;
import ar.com.ultrafibra.intranet.responses.JWTResponseRest;
import ar.com.ultrafibra.intranet.security.JwtUtil;
import ar.com.ultrafibra.intranet.security.UserDetailsServiceImp;
import ar.com.ultrafibra.intranet.service.iLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoginServiceImpl implements iLoginService {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImp userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    public ResponseEntity<JWTResponseRest> login(String username, String password) {
        JWTResponseRest response = new JWTResponseRest();

        try {
            UserDetails user = loadUser(username);
            if (isPasswordValid(password, user.getPassword())) {
                return createSuccessfulLoginResponse(user, response);
            } else {
                return createFailedLoginResponse("Credenciales incorrectas", HttpStatus.BAD_REQUEST);
            }
        } catch (UsernameNotFoundException e) {
            return createFailedLoginResponse("Usuario no encontrado", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error inesperado durante el inicio de sesión", e);
            return createFailedLoginResponse("Error al intentar acceder al servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private UserDetails loadUser(String username) {
        return userDetailsService.loadUserByUsername(username);
    }

    private boolean isPasswordValid(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private ResponseEntity<JWTResponseRest> createSuccessfulLoginResponse(UserDetails user, JWTResponseRest response) {
        List<String> roles = getUserRoles(user);
        String token = generateJwtToken(user.getUsername());

        Jwt jwt = new Jwt(token, jwtUtil.getExpiresDate());
        response.getJwtResponse().setJwt(List.of(jwt));
        response.setRoles(roles);
        response.setMetadata("Respuesta ok", "00", "Login correcto");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<String> getUserRoles(UserDetails user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private String generateJwtToken(String username) {
        return jwtUtil.encode(username);
    }

    private ResponseEntity<JWTResponseRest> createFailedLoginResponse(String errorMessage, HttpStatus status) {
        JWTResponseRest response = new JWTResponseRest();
        response.setMetadata("Respuesta nok", "-1", errorMessage);
        return new ResponseEntity<>(response, status);
    }
}


/*
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
*/