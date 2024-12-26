package ar.com.ultrafibra.intranet.dao;

import ar.com.ultrafibra.intranet.entities.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface iPaymentDao extends JpaRepository<Payment, Long> {

    public Payment findByIdRealSoftware(String id);
    
    public boolean existsByIdRealSoftware(String id);

    @Query("SELECT p FROM Payment p WHERE p.payment_date LIKE CONCAT('%-', :monthYear)")
    public List<Payment> findByMonthYear(@Param("monthYear") String monthYear);
}
