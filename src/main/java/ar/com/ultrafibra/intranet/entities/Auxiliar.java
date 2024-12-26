package ar.com.ultrafibra.intranet.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Data;

@Entity
@Data
@Table(name = "auxiliar")
public class Auxiliar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auxiliar_key")
    private String auxiliarKey;

    private long value = 0;

    private String descripcion;

    private Date lastRecordUploaded;
}
