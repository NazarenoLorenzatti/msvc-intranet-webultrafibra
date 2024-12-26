package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.Contract;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface iContractDao extends JpaRepository<Contract, Long>{
    
    public Optional<Contract> findByIdRealSoft(String idRealSoft);
    public boolean existsByIdRealSoft(String idRealSoft);
    public List<Contract> findByBajaTrue();
    
}
