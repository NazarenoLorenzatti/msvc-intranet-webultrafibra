package ar.com.ultrafibra.intranet.responses;

import ar.com.ultrafibra.intranet.entities.Jwt;
import java.util.List;
import lombok.Data;

@Data
public class JWTResponse  {
    private List<Jwt> jwt;
}
