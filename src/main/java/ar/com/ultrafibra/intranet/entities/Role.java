package ar.com.ultrafibra.intranet.entities;

import jakarta.persistence.*;
import java.io.Serializable;

import lombok.Data;

@Entity
@Data
@Table(name = "rol")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRol;

    private String role;

}
