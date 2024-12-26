package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface iRolesDao extends JpaRepository<Role, Long> {
    
    public Role findByRole(String role);
}
