package main.controller;

import main.api.response.checkAuthorization.CheckAuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final CheckAuthResponse checkAuthResponse;

    public ApiAuthController(CheckAuthResponse checkAuthResponse) {
        this.checkAuthResponse = checkAuthResponse;
    }

    @GetMapping("/check")
    private ResponseEntity<CheckAuthResponse> checkAuth(){
        checkAuthResponse.setResult(false);
        return new ResponseEntity<>(checkAuthResponse, HttpStatus.OK);
    }
}
