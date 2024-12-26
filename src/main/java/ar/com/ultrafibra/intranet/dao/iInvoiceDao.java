package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.Invoice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface iInvoiceDao extends JpaRepository<Invoice, Long>{
    
    public Optional<Invoice> findByIdRealSoftware(String idRealSoft);
    
    public Optional<Invoice> findByTypeAndSalesPointAndNumberInvoice(String type, String salesPoint, long numberInvoice);
    
    
}
