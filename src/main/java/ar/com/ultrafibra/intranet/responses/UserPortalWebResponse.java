package ar.com.ultrafibra.intranet.responses;

import ar.com.ultrafibra.intranet.entities.UserPortalWeb;
import java.util.List;
import lombok.Data;

@Data
public class UserPortalWebResponse {
    private List<UserPortalWeb> users;
}
