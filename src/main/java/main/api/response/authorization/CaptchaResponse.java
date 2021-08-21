package main.api.response.authorization;

import lombok.Data;

@Data
public class CaptchaResponse {

    private String secret;
    private String image;

    public CaptchaResponse() {
    }

    public CaptchaResponse(String secret, String image) {
        this.secret = secret;
        this.image = image;
    }
}
