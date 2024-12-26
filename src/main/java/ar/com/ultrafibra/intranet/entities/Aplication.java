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
@Table(name = "aplications")
public class Aplication {
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aplication_id;
    
    private String idRealSoftware;
    private String date_of_aplication;
    private double amount;
    private String type;
    private String sales_point; 
    private String number_comprobant;
    
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "payment_id")
    private Payment payment;
}
