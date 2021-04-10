package main.config;

import main.api.response.InitResponse;
import main.api.response.authorization.LoginResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public InitResponse initResponse() {
        return new InitResponse();
    }

    @Bean
    public LoginResponse loginResponse() {
        return new LoginResponse();
    }
}
