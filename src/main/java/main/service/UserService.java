package main.service;

import main.api.request.ProfileImageRequest;
import main.api.request.ProfileRequest;
import main.api.request.registration.LoginRequest;
import main.api.request.registration.RegisterRequest;
import main.api.response.StatisticsResponse;
import main.api.response.authorization.LoginResponse;
import main.api.response.authorization.UserResponse;
import main.model.entity.CaptchaCode;
import main.model.entity.User;
import main.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CaptchaCodeService captchaCodeService;
    @PersistenceContext
    EntityManager em;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, CaptchaCodeService captchaCodeService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.captchaCodeService = captchaCodeService;
    }

    public List<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public List<User> findByCode(String code){
        return userRepository.findByCode(code);
    }

    public User saveUser(RegisterRequest registerRequest) {
        User user = new User(registerRequest.getName(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getEmail());
        userRepository.save(user);
        return user;
    }

    public User saveUser(User user) {
        userRepository.save(user);
        return user;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication auth = authenticationManager.
                authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        String username = ((UserDetails) auth.getPrincipal()).getUsername();
        return getLoginResponse(username);
    }

    public Map<String, String> checkUserData(RegisterRequest registerRequest) {
        Map<String, String> errors = new HashMap<>();

        List<User> users = findByEmail(registerRequest.getEmail());
        if (!users.isEmpty()){
            errors.put("email", "Этот e-mail уже зарегистрирован");
        }

        if (!registerRequest.getName().matches("[а-яА-Я\\-\\s]+$")){
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

    public Map<String, String> checkUserData(ProfileRequest profileRequest, User user) {
        Map<String, String> errors = new HashMap<>();

        if (!user.getEmail().equals(profileRequest.getEmail())) {
            List<User> users = findByEmail(profileRequest.getEmail());
            if (!users.isEmpty()) {
                errors.put("email", "Этот e-mail уже зарегистрирован");
            }
        }

        if (!profileRequest.getName().matches("[а-яА-Я\\-\\s]+$")){
            errors.put("name", "Имя указано неверно");
        }

        if (profileRequest.getPassword() != null && profileRequest.getPassword().length() < 6){
            errors.put("password", "Пароль короче 6-ти символов");
        }

        return errors;
    }

    public Map<String, String> checkUserData(ProfileImageRequest profileRequest, User user) {
        Map<String, String> errors = new HashMap<>();

        if (!user.getEmail().equals(profileRequest.getEmail())) {
            List<User> users = findByEmail(profileRequest.getEmail());
            if (!users.isEmpty()) {
                errors.put("email", "Этот e-mail уже зарегистрирован");
            }
        }

        if (!profileRequest.getName().matches("[а-яА-Я\\-\\s]+$")){
            errors.put("name", "Имя указано неверно");
        }

        if (profileRequest.getPassword() != null && profileRequest.getPassword().length() < 6){
            errors.put("password", "Пароль короче 6-ти символов");
        }

        return errors;
    }

    public String encodePassword(String password){
        return passwordEncoder.encode(password);
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

    public StatisticsResponse getUserStatistics(){
        List<StatisticsResponse> resultQueryUserStatistic = getStatisticsResponseFromQuery(getCurrentUser());
        if (resultQueryUserStatistic.isEmpty()) {
            return new StatisticsResponse();
        }
        else {
            StatisticsResponse myStatisticsResponse =  resultQueryUserStatistic.get(0);
            List<Tuple> votesCountUser = getVotesCount(getCurrentUser());
            votesCountUser.forEach(res -> {
                myStatisticsResponse.setLikesCount(Math.toIntExact((Long) res.get("likesCount")));
                myStatisticsResponse.setDislikesCount(Math.toIntExact((Long) res.get("dislikesCount")));
            });

            return myStatisticsResponse;
        }
    }

    private List<StatisticsResponse> getStatisticsResponseFromQuery(User user) {
        return em
                .createQuery( "SELECT NEW main.api.response.StatisticsResponse(COUNT(DISTINCT p.id) AS postsCount, " +
                        "0 AS likesCount, " +
                        "0 AS dislikesCount, " +
                        "SUM(p.viewCount) AS viewsCount, " +
                        "MIN(UNIX_TIMESTAMP(p.time)) AS firstPublication) " +
                        "FROM User u " +
                        "LEFT JOIN Post as p ON p.user = u " +
                        "WHERE p.user = :user " +
                        "GROUP BY p.user", StatisticsResponse.class)
                .setParameter("user", user)
                .getResultList();
    }

    private List<Tuple> getVotesCount(User user){
        return em
                .createQuery( "SELECT SUM(CASE WHEN votes.value = 1 THEN 1 ELSE 0 END) AS likesCount, " +
                        "SUM(CASE WHEN votes.value = -1 THEN 1 ELSE 0 END) AS dislikesCount " +
                        "FROM User u " +
                        "INNER JOIN PostVote AS votes ON u = votes.user " +
                        "WHERE u = :user " +
                        "GROUP BY u", Tuple.class)
                .setParameter("user", user)
                .getResultList();
    }

    public StatisticsResponse getAllStatistics(){
        List<StatisticsResponse> resultQueryUserStatistic = getStatisticsResponseFromQuery();
        if (resultQueryUserStatistic.isEmpty()) {
            return new StatisticsResponse();
        }
        else {
            StatisticsResponse statisticsResponse =  resultQueryUserStatistic.get(0);
            List<Tuple> votesCountUser = getVotesCount();
            votesCountUser.forEach(res -> {
                statisticsResponse.setLikesCount(Math.toIntExact((Long) res.get("likesCount")));
                statisticsResponse.setDislikesCount(Math.toIntExact((Long) res.get("dislikesCount")));
            });

            return statisticsResponse;
        }
    }

    private List<StatisticsResponse> getStatisticsResponseFromQuery() {
        return em
                .createQuery( "SELECT NEW main.api.response.StatisticsResponse(COUNT(DISTINCT p.id) AS postsCount, " +
                        "0 AS likesCount, " +
                        "0 AS dislikesCount, " +
                        "SUM(p.viewCount) AS viewsCount, " +
                        "MIN(UNIX_TIMESTAMP(p.time)) AS firstPublication) " +
                        "FROM Post as p ", StatisticsResponse.class)
                .getResultList();
    }

    private List<Tuple> getVotesCount(){
        return em
                .createQuery( "SELECT SUM(CASE WHEN votes.value = 1 THEN 1 ELSE 0 END) AS likesCount, " +
                        "SUM(CASE WHEN votes.value = -1 THEN 1 ELSE 0 END) AS dislikesCount " +
                        "FROM PostVote AS votes ", Tuple.class)
                .getResultList();
    }

}
