package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.iUserPortalWebDao;
import ar.com.ultrafibra.intranet.entities.UserPortalWeb;
import ar.com.ultrafibra.intranet.responses.UserPortalWebResponseRest;
import ar.com.ultrafibra.intranet.service.iUserPortalWebService;
import ar.com.ultrafibra.intranet.service.util.FindClientRealSoftware;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
