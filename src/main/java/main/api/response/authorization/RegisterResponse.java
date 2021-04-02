package main.api.response.authorization;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterResponse {
    boolean result;
    Map<String, String> errors;

    public RegisterResponse() {
    }

    public RegisterResponse(boolean result) {
        this.result = result;
    }

    public RegisterResponse(boolean result, Map<String, String> errors) {
        this.result = result;
        this.errors = errors;
    }
}
