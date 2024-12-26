package ar.com.ultrafibra.intranet.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String idRealSoftware;

    private String ticketType;
    private String status;
    private String creation_date;
    private String create_by;
    private String last_modification_date;
    private String modified_by;
    private String assigned; // Grupo asignado
    private boolean os_assigned;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "id_client")
    private Client client;

}
