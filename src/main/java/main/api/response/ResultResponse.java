package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultResponse {
    Boolean result;
    Map<String, String> errors;
    Integer id;

    public ResultResponse() {
    }

    public ResultResponse(Boolean result) {
        this.result = result;
    }

    public ResultResponse(Boolean result, Map<String, String> errors) {
        this.result = result;
        this.errors = errors;
    }

    public ResultResponse(Integer id) {
        this.id = id;
    }
}
