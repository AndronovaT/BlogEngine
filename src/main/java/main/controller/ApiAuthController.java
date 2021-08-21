package main.controller;

import main.api.request.ProfileRequest;
import main.api.request.registration.ChangePasswordRequest;
import main.api.request.registration.LoginRequest;
import main.api.request.registration.RegisterRequest;
import main.api.response.authorization.LoginResponse;
import main.api.response.authorization.CaptchaResponse;
import main.api.response.ResultResponse;
import main.service.CaptchaCodeService;
import main.service.UserService;
import main.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;



@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthService authService;
    private final CaptchaCodeService captchaCodeService;
    private final UserService userService;


    public ApiAuthController(AuthService authService, CaptchaCodeService captchaCodeService, UserService userService) {
        this.authService = authService;
        this.captchaCodeService = captchaCodeService;
        this.userService = userService;
    }

    @GetMapping("/check")
    public ResponseEntity<LoginResponse> checkAuth(Principal principal){
        
        if (principal == null){
            return new ResponseEntity<>(new LoginResponse(), HttpStatus.OK);
        }
        
        LoginResponse loginResponse = userService.getLoginResponse(principal.getName());
        
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }

    @GetMapping("/captcha")
    public ResponseEntity<CaptchaResponse> getCaptcha(){
        
        return captchaCodeService.createResponseCaptcha();
        
    }

    @PostMapping("/register")
    public ResponseEntity<ResultResponse> registerUser(@RequestBody RegisterRequest registerRequest){

        return authService.createResponseRegisterUser(registerRequest);

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){

        return new ResponseEntity<>(userService.login(loginRequest), HttpStatus.OK);

    }

    @GetMapping("/logout")
    public ResponseEntity<LoginResponse> logout(){

        return new ResponseEntity<>(new LoginResponse(true), HttpStatus.OK);

    }

    @PostMapping("/restore")
    public ResponseEntity<ResultResponse> restorePassword(@RequestBody ProfileRequest profileRequest){

        return authService.createResponseRestorePassword(profileRequest);
    }

    @PostMapping("/password")
    public ResponseEntity<ResultResponse> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest){

        return authService.changePasswordResponse(changePasswordRequest);

    }


}
