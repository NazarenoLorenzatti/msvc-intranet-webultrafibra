package ar.com.ultrafibra.intranet.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    private String username;

    private String password;
    
    private String name;
     
    @ManyToMany()
    @JoinTable(
            name = "usuario_has_rol",
            joinColumns = @JoinColumn(name = "usuario_id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "rol_id_rol")
    )
    private List<Role> roles;

}
