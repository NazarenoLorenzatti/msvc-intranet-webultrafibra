package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.AplicationsHasInvoices;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface iAplicationsHasInvoicesDao extends JpaRepository<AplicationsHasInvoices, Long>{
    
      public boolean existsByIdRealSoftPayment(String idRealSoftPayment);
    
      public Optional<AplicationsHasInvoices> findByIdRealSoftPayment(String idRealSoftPayment);
}
