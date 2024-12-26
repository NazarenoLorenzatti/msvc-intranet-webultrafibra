package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface iClientDao  extends JpaRepository<Client, Long>{
    
    public Optional<Client> findByIdRealSoft(Long idRealSoft);
    public boolean existsByIdRealSoft(Long idRealSoft);
    public Optional<Client> findTopByOrderByIdRealSoftDesc();
}
