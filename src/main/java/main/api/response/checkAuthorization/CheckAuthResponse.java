package main.api.response.checkAuthorization;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import main.api.response.checkAuthorization.UserAuthResponse;
import org.springframework.stereotype.Component;

@Data
@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckAuthResponse {
    private boolean result;
    private UserAuthResponse userAuthResponse;
}
