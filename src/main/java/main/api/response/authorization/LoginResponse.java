package main.api.response.authorization;

import lombok.Data;

@Data
public class LoginResponse {
    private boolean result;
    private UserResponse user;
}
