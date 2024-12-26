package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.UserPortalWeb;
import java.util.List;


public interface iUserPortalWebDao {

    public boolean addUser(UserPortalWeb userWeb);

    public List<UserPortalWeb> getAllUsers();

    public UserPortalWeb getUserById(Long id);
    
    public UserPortalWeb getUserByIdentityNumber(String identityNumber);

    public boolean updateUser(UserPortalWeb userWeb);

    public boolean deleteUserById(Long id);
}
