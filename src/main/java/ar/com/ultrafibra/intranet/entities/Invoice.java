package ar.com.ultrafibra.intranet.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idRealSoftware;

    private String type; // Tipo de Comprobante
    
    @Column(name="number_invoice")
    private long numberInvoice;
    
    @Column(name="sales_point")
    private String salesPoint; // Puento de venta de AFIP
    
    private String invoice_date;
    private String expiration_date;
    private String oficilization_date;
    private String client_name;
    private String client_type; //Consumidor Final - Responsable inscripto ETC
    private String city;
    private double mount; // Monto sin IVA
    private double mount_iva; // Monto con IVA
    private String productType;
    private boolean inDebit; // Esta en debito automatico?
    private String value_debit = "0"; // Porcentaje de descuento del debito automatico
}
