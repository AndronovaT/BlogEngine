package main.service;

import main.api.request.registration.LoginRequest;
import main.api.request.registration.RegisterRequest;
import main.api.response.authorization.LoginResponse;
import main.api.response.authorization.UserResponse;
import main.model.entity.User;
import main.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public List<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public User saveUser(RegisterRequest registerRequest) {
        User user = new User(registerRequest.getName(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getEmail());
        userRepository.save(user);
        return user;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication auth = authenticationManager.
                authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) auth.getPrincipal();

        return getLoginResponse(user.getUsername());
    }

    public LoginResponse getLoginResponse(String username) {
        List<User> usersByEmail = userRepository.findByEmail(username);
        if (usersByEmail.isEmpty()){
            throw new UsernameNotFoundException(username);
        }

        User currentUser = usersByEmail.get(0);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(currentUser.getId());
        userResponse.setName(currentUser.getName());
        userResponse.setPhoto(currentUser.getPhoto());
        userResponse.setEmail(currentUser.getEmail());
        userResponse.setModeration(currentUser.getIsModerator() == 1);
        userResponse.setModerationCount(0);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setResult(true);
        loginResponse.setUser(userResponse);
        return loginResponse;
    }
}
