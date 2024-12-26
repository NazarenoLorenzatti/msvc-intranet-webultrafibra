package ar.com.ultrafibra.intranet.responses;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JWTResponseRest extends ResponseRest{
    private JWTResponse jwtResponse = new JWTResponse();
    private List<String> roles;
}
