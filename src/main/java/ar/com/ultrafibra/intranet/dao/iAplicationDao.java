package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.Aplication;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface iAplicationDao extends JpaRepository<Aplication, Long>{
    
    public Optional<Aplication> findByIdRealSoftware(String idRealSoftware);
    
     public boolean existsByIdRealSoftware(String idRealSoftware);
}
