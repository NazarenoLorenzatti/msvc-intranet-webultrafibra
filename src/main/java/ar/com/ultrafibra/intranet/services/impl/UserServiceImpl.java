package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.iRolesDao;
import ar.com.ultrafibra.intranet.dao.iUserDao;
import ar.com.ultrafibra.intranet.entities.Role;
import ar.com.ultrafibra.intranet.entities.User;
import ar.com.ultrafibra.intranet.responses.UserResponseRest;
import ar.com.ultrafibra.intranet.service.iUserService;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Slf4j
@Data
@Service
public class UserServiceImpl implements iUserService {

    @Autowired
    private iUserDao userDao;

    @Autowired
    private iRolesDao roleDao;

    private static final String DEFAULT_ROLE = "ROLE_SUPPORT";

    public static String encryptPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    private List<Role> getRolesForUser(User user) {
        List<Role> roles = new ArrayList<>();
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> roles.add(roleDao.findByRole(role.getRole())));
        } else {
            roles.add(roleDao.findByRole(DEFAULT_ROLE));
        }
        return roles;
    }

    @Override
    public ResponseEntity<UserResponseRest> saveUser(User user) {
        UserResponseRest response = new UserResponseRest();

        if (user == null) {
            return createErrorResponse(response, "Error en la Consulta", HttpStatus.BAD_REQUEST);
        }

        try {
            user.setPassword(encryptPassword(user.getPassword()));
            user.setRoles(getRolesForUser(user));
            User newUser = userDao.save(user);

            if (newUser != null) {
                response.getUserResponse().setUsers(List.of(newUser));
                response.setMetadata("Ok", "00", "Usuario Guardado");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return createErrorResponse(response, "No se pudo Guardar el nuevo usuario", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error al guardar usuario", e);
            return createErrorResponse(response, "No se pudo Guardar el nuevo usuario, error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserResponseRest> editUser(User user) {
        UserResponseRest response = new UserResponseRest();

        if (user == null) {
            return createErrorResponse(response, "Error en la Consulta", HttpStatus.BAD_REQUEST);
        }

        try {
            user.setPassword(encryptPassword(user.getPassword()));
            user.setRoles(getRolesForUser(user));
            User updatedUser = userDao.save(user);

            if (updatedUser != null) {
                response.getUserResponse().setUsers(List.of(updatedUser));
                response.setMetadata("Ok", "00", "Usuario Guardado");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return createErrorResponse(response, "No se pudo Guardar el nuevo usuario", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error al editar usuario", e);
            return createErrorResponse(response, "No se pudo Editar el usuario, error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserResponseRest> deleteUser(Long id) {
        UserResponseRest response = new UserResponseRest();

        if (id == null) {
            return createErrorResponse(response, "Error en la Consulta", HttpStatus.BAD_REQUEST);
        }

        try {
            userDao.deleteById(id);
            response.setMetadata("Ok", "00", "Usuario Eliminado");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al eliminar usuario", e);
            return createErrorResponse(response, "No se pudo eliminar el usuario, error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserResponseRest> findAllUsers() {
        UserResponseRest response = new UserResponseRest();

        try {
            List<User> users = userDao.findAll();

            if (users.isEmpty()) {
                return createErrorResponse(response, "No se encontraron usuarios", HttpStatus.NOT_FOUND);
            }

            response.getUserResponse().setUsers(users);
            response.setMetadata("Ok", "00", "Lista de usuarios");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al obtener usuarios", e);
            return createErrorResponse(response, "Error en el servidor al obtener usuarios", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserResponseRest> findUser(String username) {
        UserResponseRest response = new UserResponseRest();

        try {
            User user = userDao.findByUsername(username);

            if (user == null) {
                return createErrorResponse(response, "Usuario no Encontrado", HttpStatus.NOT_FOUND);
            }

            response.getUserResponse().setUsers(List.of(user));
            response.setMetadata("Ok", "00", "Usuario encontrado");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al buscar usuario", e);
            return createErrorResponse(response, "Error en el servidor al buscar usuario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<UserResponseRest> createErrorResponse(UserResponseRest response, String message, HttpStatus status) {
        response.setMetadata("nOk", "01", message);
        return new ResponseEntity<>(response, status);
    }
}

