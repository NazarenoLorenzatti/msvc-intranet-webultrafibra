package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.ServiceOrder;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface iServiceOrderDao extends JpaRepository<ServiceOrder, Long> {

    public List<ServiceOrder> findByRegisterDateBetween(Date startDate, Date endDate);

    public Optional<ServiceOrder> findByIdRealSoft(String idRealSoft);

    public boolean existsByIdRealSoft(String idRealSoft);
    
    public void deleteByIdRealSoft(String idRealSoft);
}
