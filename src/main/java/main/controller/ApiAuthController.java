package main.controller;

import main.api.request.registration.RegisterRequest;
import main.api.response.authorization.CaptchaResponse;
import main.api.response.authorization.CheckAuthResponse;
import main.api.response.authorization.RegisterResponse;
import main.model.entity.CaptchaCode;
import main.model.entity.User;
import main.service.CaptchaCodeService;
import main.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final CheckAuthResponse checkAuthResponse;
    private final CaptchaCodeService captchaCodeService;
    private final UserService userService;

    public ApiAuthController(CheckAuthResponse checkAuthResponse, CaptchaCodeService captchaCodeService, UserService userService) {
        this.checkAuthResponse = checkAuthResponse;
        this.captchaCodeService = captchaCodeService;
        this.userService = userService;
    }

    @GetMapping("/check")
    private ResponseEntity<CheckAuthResponse> checkAuth(){
        checkAuthResponse.setResult(false);
        return new ResponseEntity<>(checkAuthResponse, HttpStatus.OK);
    }

    @GetMapping("/captcha")
    private ResponseEntity<CaptchaResponse> getCaptcha(){
        captchaCodeService.deleteOldCaptcha();
        return new ResponseEntity<>(captchaCodeService.generateCaptcha(), HttpStatus.OK);
    }

    @PostMapping("/register")
    private ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest registerRequest){
        Map<String, String> errors = checkRegData(registerRequest);

        if (errors.size() > 0){
            return new ResponseEntity<>(new RegisterResponse(false, errors), HttpStatus.OK);
        }

        userService.saveUser(registerRequest);

        return new ResponseEntity<>(new RegisterResponse(true), HttpStatus.OK);
    }

    private Map<String, String> checkRegData(@RequestParam RegisterRequest registerRequest) {
        Map<String, String> errors = new HashMap<>();

        List<User> users = userService.findByEmail(registerRequest.getEmail());
        if (users != null && users.size() > 0){
            errors.put("email", "Этот e-mail уже зарегистрирован");
        }

        if (!registerRequest.getName().matches("[а-яА-Я\\-]+$")){
            errors.put("name", "Имя указано неверно");
        }

        if (registerRequest.getPassword().length() < 6){
            errors.put("password", "Пароль короче 6-ти символов");
        }

        List<CaptchaCode> captchaCode = captchaCodeService.findCaptchaBySecret(registerRequest.getCaptchaSecret());
        if (captchaCode == null || captchaCode.size() == 0) {
            errors.put("captcha", "Код с картинки введён неверно");
        } else if (!captchaCode.get(0).getCode().equals(registerRequest.getCaptcha())) {
            errors.put("captcha", "Код с картинки введён неверно");
        }
        return errors;
    }
}
