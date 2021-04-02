package main.service;

import main.api.request.registration.RegisterRequest;
import main.model.entity.User;
import main.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public User saveUser(RegisterRequest registerRequest) {
        User user = new User(registerRequest.getName(), registerRequest.getPassword(), registerRequest.getEmail());
        userRepository.save(user);
        return user;
    }
}
