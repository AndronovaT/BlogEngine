package main.service;

import main.api.request.registration.LoginRequest;
import main.api.request.registration.RegisterRequest;
import main.api.response.MyStatisticsResponse;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    @PersistenceContext
    EntityManager em;

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

    public User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        return getUser(currentPrincipalName);

    }

    private User getUser(String username) {
        List<User> usersByEmail = userRepository.findByEmail(username);
        if (usersByEmail.isEmpty()){
            throw new UsernameNotFoundException(username);
        }

        return usersByEmail.get(0);
    }

    public LoginResponse getLoginResponse(String username) {
        User currentUser = getUser(username);

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

    public MyStatisticsResponse getUserStatistics(){
        List<MyStatisticsResponse> resultQueryUserStatistic = getMyStatisticsResponseFromQuery(getCurrentUser());
        if (resultQueryUserStatistic.isEmpty()) {
            return new MyStatisticsResponse();
        }
        else {
            MyStatisticsResponse myStatisticsResponse =  resultQueryUserStatistic.get(0);
            List<Long> viewCountUserPosts = getViewCountUserPosts(getCurrentUser());
            if (!viewCountUserPosts.isEmpty()){
                myStatisticsResponse.setViewsCount(viewCountUserPosts.get(0));
            }
            return myStatisticsResponse;
        }
    }

    private List<MyStatisticsResponse> getMyStatisticsResponseFromQuery(User user) {
        return em
                .createQuery( "SELECT NEW main.api.response.MyStatisticsResponse(COUNT(DISTINCT p.id) AS postsCount, " +
                        "SUM(CASE WHEN likes.value IS NOT NULL THEN 1 ELSE 0 END) AS likesCount, " +
                        "SUM(CASE WHEN dislikes.value IS NOT NULL THEN 1 ELSE 0 END) AS dislikesCount, " +
                        "SUM(p.viewCount) AS viewsCount, " +
                        "MIN(UNIX_TIMESTAMP(p.time)) AS firstPublication) " +
                        "FROM Post as p " +
                        "LEFT JOIN PostVote AS likes ON p = likes.post AND likes.value = 1 " +
                        "LEFT JOIN PostVote AS dislikes ON p = dislikes.post AND dislikes.value = -1 " +
                        "WHERE p.user = :user " +
                        "GROUP BY p.user", MyStatisticsResponse.class)
                .setParameter("user", user)
                .getResultList();
    }

    private List<Long> getViewCountUserPosts(User user){
        return em
                .createQuery( "SELECT SUM(p.viewCount) AS viewsCount " +
                        "FROM Post as p " +
                        "WHERE p.user = :user " +
                        "GROUP BY p.user", Long.class)
                .setParameter("user", user)
                .getResultList();
    }

}
