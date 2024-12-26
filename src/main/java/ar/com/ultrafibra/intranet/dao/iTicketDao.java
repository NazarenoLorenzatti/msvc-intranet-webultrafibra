package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.Ticket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface iTicketDao extends JpaRepository<Ticket, Long> {

    public List<Ticket> findByStatus(String status);

    public Optional<Ticket> findByIdRealSoftware(String idRealSoft);

    public boolean existsByIdRealSoftware(String idRealSoft);
    
    public void deleteByIdRealSoftware(String idRealSoft);
}
