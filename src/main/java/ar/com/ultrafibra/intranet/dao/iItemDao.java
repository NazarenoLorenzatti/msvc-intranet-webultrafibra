package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.Item;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface iItemDao extends JpaRepository<Item, Long>{
    
        
    public Optional<Item> findByIdRealSoftware(String idRealSoftware);
    
     public boolean existsByIdRealSoftware(String idRealSoftware);
}
