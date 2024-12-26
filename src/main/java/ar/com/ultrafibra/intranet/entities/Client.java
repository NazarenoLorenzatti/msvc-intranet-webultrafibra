package ar.com.ultrafibra.intranet.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Data;


@Entity
@Data
@Table(name = "clients")
public class Client {
            
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_client;
    
    private Long idRealSoft;
    private String name;
    private String dni;
    private String city;
    private String status;
    private String address;
    private String district;
    private String wallet;
    private boolean corpo;
    private boolean automatic_debt;
    private String filial;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Contract> contracts;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOrder> serviceOrders;
}
