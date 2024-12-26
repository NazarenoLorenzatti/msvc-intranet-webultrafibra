package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface iUserDao extends JpaRepository<User, Long>{
        User findByUsername(String username);
    
    public boolean existsByUsername(String username);   
}
