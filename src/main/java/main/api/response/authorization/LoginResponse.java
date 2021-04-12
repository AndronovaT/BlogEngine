package main.api.response.authorization;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private boolean result;
    private UserResponse user;

    public LoginResponse(boolean result) {
        this.result = result;
    }
}
