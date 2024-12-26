package ar.com.ultrafibra.intranet.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "aplications_has_invoices")
public class AplicationsHasInvoices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;
    private String type;
    private String sales_point; 
    private String number_comprobant;
    private String invoice_date;
    private String application_date;
    private String payment_date;
    private String idRealSoftInvoice;
    private String idRealSoftPayment;
    private boolean isApplied;
    
    
}
