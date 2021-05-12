package main.controller;

import main.api.request.ProfileRequest;
import main.api.request.registration.ChangePasswordRequest;
import main.api.request.registration.LoginRequest;
import main.api.request.registration.RegisterRequest;
import main.api.response.InitResponse;
import main.api.response.SettingsResponse;
import main.api.response.authorization.LoginResponse;
import main.api.response.authorization.CaptchaResponse;
import main.api.response.ResultResponse;
import main.model.entity.CaptchaCode;
import main.model.entity.User;
import main.service.CaptchaCodeService;
import main.service.SettingsService;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final LoginResponse loginResponse;
    private final CaptchaCodeService captchaCodeService;
    private final UserService userService;
    private final InitResponse initResponse;
    private final SettingsService settingsService;
    @Autowired
    private MailSender emailSender;

    public ApiAuthController(LoginResponse loginResponse, CaptchaCodeService captchaCodeService, UserService userService,
                             InitResponse initResponse, SettingsService settingsService) {
        this.loginResponse = loginResponse;
        this.captchaCodeService = captchaCodeService;
        this.userService = userService;
        this.initResponse = initResponse;
        this.settingsService = settingsService;
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
        if (!settingsService.getGlobalSettings().isMultiuserMode()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Map<String, String> errors = userService.checkUserData(registerRequest);

        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.OK);
        }

        userService.saveUser(registerRequest);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
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
        List<User> userByEmail = userService.findByEmail(profileRequest.getEmail());
        if (userByEmail.isEmpty()){
            return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
        }
        String hash = UUID.randomUUID().toString().replaceAll("-", "");

        final SimpleMailMessage simpleMail = new SimpleMailMessage();
        simpleMail.setFrom(initResponse.getEmail());
        simpleMail.setTo(profileRequest.getEmail());
        simpleMail.setSubject(initResponse.getTitle() + " " + initResponse.getSubtitle() + "- восстановление пароля");
        simpleMail.setText("<a href=/login/change-password/" + hash + ">");

        emailSender.send(simpleMail);

        User user = userByEmail.get(0);
        user.setCode(hash);
        userService.saveUser(user);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }

    @PostMapping("/password")
    public ResponseEntity<ResultResponse> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest){

        Map<String, String> errors = new HashMap<>();

        List<User> users = userService.findByCode(changePasswordRequest.getCode());
        if (users.isEmpty()){
            errors.put("code", "Ссылка для восстановления пароля устарела.\n" +
                    "<a href=\n" +
                    "\"/auth/restore\">Запросить ссылку снова</a>");
        }

        List<CaptchaCode> captchaCode = captchaCodeService.findCaptchaBySecret(changePasswordRequest.getCaptchaSecret());
        if (captchaCode.isEmpty() || !captchaCode.get(0).getCode().equals(changePasswordRequest.getCaptcha())) {
            errors.put("captcha", "Код с картинки введён неверно");
        }

        if (changePasswordRequest.getPassword().length() < 6){
            errors.put("password", "Пароль короче 6-ти символов");
        }

        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.OK);
        }

        User currentUser = users.get(0);
        currentUser.setPassword(userService.encodePassword(changePasswordRequest.getPassword()));
        userService.saveUser(currentUser);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }
}
