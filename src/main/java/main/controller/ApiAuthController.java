package main.controller;

import main.api.response.checkAuthorization.CaptchaResponse;
import main.api.response.checkAuthorization.CheckAuthResponse;
import main.service.CaptchaCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final CheckAuthResponse checkAuthResponse;
    private final CaptchaCodeService captchaCodeService;

    public ApiAuthController(CheckAuthResponse checkAuthResponse, CaptchaCodeService captchaCodeService) {
        this.checkAuthResponse = checkAuthResponse;
        this.captchaCodeService = captchaCodeService;
    }

    @GetMapping("/check")
    private ResponseEntity<CheckAuthResponse> checkAuth(){
        checkAuthResponse.setResult(false);
        return new ResponseEntity<>(checkAuthResponse, HttpStatus.OK);
    }

    @GetMapping("/captcha")
    private ResponseEntity<CaptchaResponse> getCaptcha(){
        return new ResponseEntity<>(captchaCodeService.generateCaptcha(), HttpStatus.OK);
    }
}
