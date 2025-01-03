package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.iUserPortalWebDao;
import ar.com.ultrafibra.intranet.entities.UserPortalWeb;
import ar.com.ultrafibra.intranet.responses.UserPortalWebResponseRest;
import ar.com.ultrafibra.intranet.service.iUserPortalWebService;
import ar.com.ultrafibra.intranet.service.util.FindClientRealSoftware;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import java.util.ArrayList;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/*
@Slf4j
@Service
public class UserPortalWebServiceImpl implements iUserPortalWebService {

    private final iUserPortalWebDao userDao;
    private final FindClientRealSoftware findClient;

    public UserPortalWebServiceImpl(@Autowired iUserPortalWebDao userDao, @Autowired FindClientRealSoftware findClient) {
        this.userDao = userDao;
        this.findClient = findClient;
    }

    @Override
    public ResponseEntity<UserPortalWebResponseRest> saveUser(UserPortalWeb userWeb) {
        if (userWeb == null) {
            return buildErrorResponse("No se ingresó un DNI", HttpStatus.BAD_REQUEST);
        }

        if (userDao.getUserByIdentityNumber(userWeb.getIdentityNumber()) != null) {
            return buildErrorResponse("El DNI ya está registrado", HttpStatus.BAD_REQUEST);
        }

        HttpResponse<JsonNode> response = findClient.getClientRealSoft(userWeb.getIdentityNumber());
        if (response == null) {
            return buildErrorResponse("Cliente no encontrado con ese DNI", HttpStatus.NOT_FOUND);
        }

        if (userDao.addUser(userWeb)) {
            return buildSuccessResponse(Collections.singletonList(userWeb), "Nuevo usuario guardado", HttpStatus.OK);
        } else {
            return buildErrorResponse("No se pudo guardar el usuario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserPortalWebResponseRest> findUser(UserPortalWeb userWeb) {
        if (userWeb == null) {
            return buildErrorResponse("No se ingresó un ID", HttpStatus.BAD_REQUEST);
        }

        UserPortalWeb userByDni = userDao.getUserByIdentityNumber(userWeb.getIdentityNumber());
        if (userByDni == null) {
            return buildErrorResponse("Usuario no encontrado", HttpStatus.BAD_REQUEST);
        }

        return buildSuccessResponse(Collections.singletonList(userByDni), "Usuario encontrado", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserPortalWebResponseRest> editUser(UserPortalWeb userWeb) {
        if (userWeb == null) {
            return buildErrorResponse("No se ingresó un DNI", HttpStatus.BAD_REQUEST);
        }

        if (userDao.getUserByIdentityNumber(userWeb.getIdentityNumber()) == null) {
            return buildErrorResponse("Usuario no encontrado para editar", HttpStatus.BAD_REQUEST);
        }

        if (userDao.updateUser(userWeb)) {
            return buildSuccessResponse(Collections.singletonList(userWeb), "Usuario editado", HttpStatus.OK);
        } else {
            return buildErrorResponse("No se pudo editar el usuario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserPortalWebResponseRest> findAll() {
        List<UserPortalWeb> users = userDao.getAllUsers();
        if (users == null || users.isEmpty()) {
            return buildErrorResponse("No se pudo obtener la lista de usuarios", HttpStatus.BAD_REQUEST);
        }

        return buildSuccessResponse(users, "Lista de usuarios", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserPortalWebResponseRest> deleteUser(Long id) {
        if (id == null) {
            return buildErrorResponse("No se ingresó un ID", HttpStatus.BAD_REQUEST);
        }

        UserPortalWeb userById = userDao.getUserById(id);
        if (userById == null) {
            return buildErrorResponse("Usuario no encontrado para eliminar", HttpStatus.BAD_REQUEST);
        }

        if (userDao.deleteUserById(id)) {
            return buildSuccessResponse(Collections.singletonList(userById), "Usuario eliminado", HttpStatus.OK);
        } else {
            return buildErrorResponse("No se pudo eliminar el usuario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<UserPortalWebResponseRest> buildSuccessResponse(List<UserPortalWeb> users, String message, HttpStatus status) {
        UserPortalWebResponseRest response = new UserPortalWebResponseRest();
        response.getUserResponse().setUsers(users);
        response.setMetadata("Ok", "00", message);
        return new ResponseEntity<>(response, status);
    }

    private ResponseEntity<UserPortalWebResponseRest> buildErrorResponse(String message, HttpStatus status) {
        UserPortalWebResponseRest response = new UserPortalWebResponseRest();
        response.setMetadata("nOk", "01", message);
        return new ResponseEntity<>(response, status);
    }
}

*/





@Slf4j
@Service
@Data
public class UserPortalWebServiceImpl implements iUserPortalWebService {

    @Autowired
    private iUserPortalWebDao userDao;

    @Autowired
    private FindClientRealSoftware findClient;

    @Override
    public ResponseEntity<UserPortalWebResponseRest> saveUser(UserPortalWeb userWeb) {
        UserPortalWebResponseRest respuesta = new UserPortalWebResponseRest();
        List<UserPortalWeb> users = new ArrayList();
        if (userWeb != null) {
            UserPortalWeb userByDni = userDao.getUserByIdentityNumber(userWeb.getIdentityNumber());
            if (userByDni == null) {
                HttpResponse<JsonNode> response = findClient.getClientRealSoft(userWeb.getIdentityNumber());
                if (response != null) {
                    boolean isSaved = userDao.addUser(userWeb);
                    if (isSaved) {
                        users.add(userWeb);
                        respuesta.getUserResponse().setUsers(users);
                        respuesta.setMetadata("Ok", "00", "Nuevo Usuario Guardado " + userWeb.getIdentityNumber());
                        return new ResponseEntity<>(respuesta, HttpStatus.OK);
                    } else {
                        respuesta.setMetadata("Ok", "01", "Usuario NO Guardado " + userWeb.getIdentityNumber());
                        return new ResponseEntity<>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                } else {
                    respuesta.setMetadata("nOk", "01", "No se Encuentra el cliente con ese DNI");
                    return new ResponseEntity<>(respuesta, HttpStatus.NOT_FOUND);
                }
            } else {
                respuesta.setMetadata("nOk", "01", "Ya se encuentra guardado el DNI");
                return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
            }
        } else {
            respuesta.setMetadata("nOk", "01", "No se pudo Guardar el nuevo usuario Por q no se ingreso un DNI");
            return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<UserPortalWebResponseRest> findUser(UserPortalWeb userWeb) {
        UserPortalWebResponseRest respuesta = new UserPortalWebResponseRest();
        List<UserPortalWeb> users = new ArrayList();
        if (userWeb != null) {
            UserPortalWeb userByDni = userDao.getUserByIdentityNumber(userWeb.getIdentityNumber());
            System.out.println("userByDni = " + userByDni.isVerified());
            if (userByDni != null) {
                users.add(userByDni);
                respuesta.getUserResponse().setUsers(users);
                respuesta.setMetadata("Ok", "00", "Usuario Encontrado " + userByDni.getIdentityNumber());
                return new ResponseEntity<>(respuesta, HttpStatus.OK);
            } else {
                respuesta.setMetadata("nOk", "01", "No se pudo Eliminar el usuario Por q no se encontro con el ID");
                return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
            }
        } else {
            respuesta.setMetadata("nOk", "01", "No se pudo Eliminar el usuario Por q no se ingreso un Id");
            return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<UserPortalWebResponseRest> editUser(UserPortalWeb userWeb) {
        UserPortalWebResponseRest respuesta = new UserPortalWebResponseRest();
        List<UserPortalWeb> users = new ArrayList();
        if (userWeb != null) {
            UserPortalWeb userByDni = userDao.getUserByIdentityNumber(userWeb.getIdentityNumber());
            if (userByDni != null) {
                boolean isSaved = userDao.updateUser(userWeb);
                if (isSaved) {
                    users.add(userWeb);
                    respuesta.getUserResponse().setUsers(users);
                    respuesta.setMetadata("Ok", "00", "Usuario Editado " + userWeb.getIdentityNumber());
                    return new ResponseEntity<>(respuesta, HttpStatus.OK);
                } else {
                    respuesta.setMetadata("Ok", "01", "Usuario NO Editado " + userWeb.getIdentityNumber());
                    return new ResponseEntity<>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                respuesta.setMetadata("nOk", "01", "No se pudo Editar el usuario Por q no se ingreso un DNI");
                return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
            }
        } else {
            respuesta.setMetadata("nOk", "01", "No se pudo Editar el usuario Por q no se ingreso un DNI");
            return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<UserPortalWebResponseRest> findAll() {
        UserPortalWebResponseRest respuesta = new UserPortalWebResponseRest();
        List<UserPortalWeb> users = userDao.getAllUsers();
        if (users != null && !users.isEmpty()) {
            respuesta.getUserResponse().setUsers(users);
            respuesta.setMetadata("Ok", "00", "Lista de usuarios");
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } else {
            respuesta.setMetadata("nOk", "01", "No se pudo obtener la lista de Usuarios");
            return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ResponseEntity<UserPortalWebResponseRest> deleteUser(Long id) {
        UserPortalWebResponseRest respuesta = new UserPortalWebResponseRest();
        List<UserPortalWeb> users = new ArrayList();
        if (id != null) {
            UserPortalWeb userByDni = userDao.getUserById(id);
            if (userByDni != null) {
                boolean isDeleted = userDao.deleteUserById(id);
                if (isDeleted) {
                    users.add(userByDni);
                    respuesta.getUserResponse().setUsers(users);
                    respuesta.setMetadata("Ok", "00", "Usuario Editado " + userByDni.getIdentityNumber());
                    return new ResponseEntity<>(respuesta, HttpStatus.OK);
                } else {
                    respuesta.setMetadata("Ok", "01", "Usuario NO Editado " + userByDni.getIdentityNumber());
                    return new ResponseEntity<>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                respuesta.setMetadata("nOk", "01", "No se pudo Eliminar el usuario Por q no se encontro con el ID");
                return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
            }
        } else {
            respuesta.setMetadata("nOk", "01", "No se pudo Eliminar el usuario Por q no se ingreso un Id");
            return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
        }
    }

}