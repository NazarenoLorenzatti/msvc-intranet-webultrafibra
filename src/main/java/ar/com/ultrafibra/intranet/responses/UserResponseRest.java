package ar.com.ultrafibra.intranet.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseRest extends ResponseRest {

    private UserResponse userResponse = new UserResponse();
    
}
