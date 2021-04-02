package main.api.response.authorization;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckAuthResponse {
    private boolean result;
    private UserAuthResponse userAuthResponse;
}
