package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultResponse {
    boolean result;
    Map<String, String> errors;

    public ResultResponse() {
    }

    public ResultResponse(boolean result) {
        this.result = result;
    }

    public ResultResponse(boolean result, Map<String, String> errors) {
        this.result = result;
        this.errors = errors;
    }
}
