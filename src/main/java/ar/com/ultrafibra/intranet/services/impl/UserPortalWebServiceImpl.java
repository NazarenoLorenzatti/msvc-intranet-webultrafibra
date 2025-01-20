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
