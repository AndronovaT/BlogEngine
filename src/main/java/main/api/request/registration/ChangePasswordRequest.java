package main.api.request.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String code;

    private String password;

    private String captcha;

    @JsonProperty("captcha_secret")
    private String captchaSecret;

}
