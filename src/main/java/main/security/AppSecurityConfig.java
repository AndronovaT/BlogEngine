package main.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.response.authorization.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AppSecurityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Autowired
    public AppSecurityConfig(PasswordEncoder passwordEncoder, @Qualifier("userDetailsServiceImpl") UserDetailsService userDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/**").permitAll()
                .anyRequest()
                .authenticated()
                .and().exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint())
                .and()
                .formLogin().disable()
                .logout()
                .logoutUrl("/api/auth/logout")
                .permitAll()
                .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
                    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                    ObjectMapper objectMapper = new ObjectMapper();
                    PrintWriter out = httpServletResponse.getWriter();
                    httpServletResponse.setContentType("application/json");
                    httpServletResponse.setCharacterEncoding("UTF-8");
                    out.print(objectMapper.writeValueAsString(new LoginResponse(true)));
                    out.flush();
                })
                .and()
                .httpBasic();
    }


    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {


        public RestAuthenticationEntryPoint() {
            super();
        }

        @Override
        public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
            String error="{ \"status\":\"FAILURE\",\"error\":{\"code\":\"401\",\"message\":\"" +"Bad credential\"} }";
            HttpServletResponse httpResponse = httpServletResponse;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getOutputStream().println(error);
        }
    }

    @Bean
    protected DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return daoAuthenticationProvider;
    }


    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
