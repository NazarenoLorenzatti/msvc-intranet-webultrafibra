package ar.com.ultrafibra.intranet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ar.com.ultrafibra.intranet.entities.ContractHistory;
import java.util.Optional;

public interface iContractHistoryDao extends JpaRepository<ContractHistory, Long>{
    
    public Optional<ContractHistory> findByIdRealSoft(String idRealSoft);
    }
