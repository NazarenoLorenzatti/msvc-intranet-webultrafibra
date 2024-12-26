package ar.com.ultrafibra.intranet.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Entity
@Data
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payment_id;

    private String idRealSoftware;

    private String client_name;
    private String clientIdRealSoftware;
    private String payment_date;
    private String collector; // Tipo de recaudador
    private String client_city;
    private String observations;
    private boolean isDeclarated = true; // Esta aplicado a una factura en Blanco
    private boolean isCanceled = false; // Tiene Contrarecibo

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Item> items;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Aplication> aplications;
}
