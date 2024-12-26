package ar.com.ultrafibra.intranet.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Data;

@Entity
@Data
@Table(name = "serviceorders")
public class ServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String idRealSoft;
    private String status;
    private String affair;
    private String city;
    private String barrio;
    private String create_date;
    private String confirmed_date;
    private String closed_date;
    private String create_by;
    private String confirmed_by;
    private String made_by;
    private String closed_by;
    private Date registerDate;
    private String assigned;
    private String type;
    private String idTicket;
    
    
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "id_client")
    private Client client;
}