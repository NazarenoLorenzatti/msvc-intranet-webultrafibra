package ar.com.ultrafibra.intranet.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idRealSoft;

    private String contract_name;
    private String comming;
    private String city;
//    private String sector;
    private String lat;
    private String lon;
    private boolean baja = false;
    private String date_of_baja;
    private String reason_baja;
    private String pre_invoice;
    private String seller;
    private String observations;
    private boolean pending_installation = false;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "id_client")
    private Client client;

}
