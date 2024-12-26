package ar.com.ultrafibra.intranet.responses;

import ar.com.ultrafibra.intranet.entities.User;
import java.util.List;
import lombok.Data;

@Data
public class UserResponse {
    private List<User> users;
}
