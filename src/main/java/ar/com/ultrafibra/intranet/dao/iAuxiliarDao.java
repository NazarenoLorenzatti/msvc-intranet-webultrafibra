package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.Auxiliar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface iAuxiliarDao extends JpaRepository<Auxiliar, Long>{
    
    public Auxiliar findByAuxiliarKey(String key);
}
