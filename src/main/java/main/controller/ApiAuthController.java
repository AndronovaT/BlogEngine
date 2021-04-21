package main.controller;

import main.api.request.registration.LoginRequest;
import main.api.request.registration.RegisterRequest;
import main.api.response.authorization.LoginResponse;
import main.api.response.authorization.CaptchaResponse;
import main.api.response.ResultResponse;
import main.model.entity.CaptchaCode;
import main.model.entity.User;
import main.service.CaptchaCodeService;
import main.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final LoginResponse loginResponse;
    private final CaptchaCodeService captchaCodeService;
    private final UserService userService;

    public ApiAuthController(LoginResponse loginResponse, CaptchaCodeService captchaCodeService, UserService userService) {
        this.loginResponse = loginResponse;
        this.captchaCodeService = captchaCodeService;
        this.userService = userService;
    }

    @GetMapping("/check")
    public ResponseEntity<LoginResponse> checkAuth(Principal principal){
        if (principal == null){
            return new ResponseEntity<>(new LoginResponse(), HttpStatus.OK);
        }
        LoginResponse loginResponse = userService.getLoginResponse(principal.getName());
        loginResponse.setResult(true);
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }

    @GetMapping("/captcha")
    public ResponseEntity<CaptchaResponse> getCaptcha(){
        captchaCodeService.deleteOldCaptcha();
        return captchaCodeService.generateCaptcha();
    }

    @PostMapping("/register")
    public ResponseEntity<ResultResponse> registerUser(@RequestBody RegisterRequest registerRequest){
        Map<String, String> errors = checkRegData(registerRequest);

        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.OK);
        }

        userService.saveUser(registerRequest);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }

    private Map<String, String> checkRegData(RegisterRequest registerRequest) {
        Map<String, String> errors = new HashMap<>();

        List<User> users = userService.findByEmail(registerRequest.getEmail());
        if (!users.isEmpty()){
            errors.put("email", "Этот e-mail уже зарегистрирован");
        }

        if (!registerRequest.getName().matches("[а-яА-Я\\-]+$")){
            errors.put("name", "Имя указано неверно");
        }

        if (registerRequest.getPassword().length() < 6){
            errors.put("password", "Пароль короче 6-ти символов");
        }

        List<CaptchaCode> captchaCode = captchaCodeService.findCaptchaBySecret(registerRequest.getCaptchaSecret());
        if (captchaCode.isEmpty() || !captchaCode.get(0).getCode().equals(registerRequest.getCaptcha())) {
            errors.put("captcha", "Код с картинки введён неверно");
        }
        return errors;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        return new ResponseEntity<>(userService.login(loginRequest), HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<LoginResponse> logout(){
        return new ResponseEntity<>(new LoginResponse(true), HttpStatus.OK);
    }

}
