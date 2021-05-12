package main.controller;

import com.mortennobel.imagescaling.ResampleOp;
import main.api.request.CommentRequest;
import main.api.request.ModerationVotesRequest;
import main.api.request.ProfileRequest;
import main.api.request.ProfileImageRequest;
import main.api.response.*;
import main.api.response.tags.AllTagsResponse;
import main.model.entity.Post;
import main.model.entity.PostComment;
import main.model.entity.User;
import main.model.enums.BlogSetting;
import main.model.enums.ModerationStatus;
import main.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


@RestController
@RequestMapping("/api")
public class ApiGeneralController {

    private final InitResponse initResponse;
    private final SettingsService settingsService;
    private final TagToPostService tagToPostService;
    private final PostService postService;
    private final UserService userService;
    private final PostCommentsService postCommentsService;

    @Autowired
    private HttpServletRequest request;

    private static final long MAX_IMAGE = 5;
    private static final String SEPARATOR = File.separator;

    public ApiGeneralController(InitResponse initResponse, SettingsService settingsService, TagToPostService tagToPostService,
                                PostService postService, UserService userService, PostCommentsService postCommentsService) {
        this.initResponse = initResponse;
        this.settingsService = settingsService;
        this.tagToPostService = tagToPostService;
        this.postService = postService;
        this.userService = userService;
        this.postCommentsService = postCommentsService;
    }

    @GetMapping("/init")
    public ResponseEntity<InitResponse> init(){
        return new ResponseEntity<>(initResponse, HttpStatus.OK);
    }

    @GetMapping("/settings")
    public ResponseEntity<SettingsResponse> settings(){
        return new ResponseEntity<>(settingsService.getGlobalSettings(), HttpStatus.OK);
    }

    @GetMapping("/tag")
    public ResponseEntity<AllTagsResponse> tags(@RequestParam(defaultValue = "") String query) {
        return new ResponseEntity(tagToPostService.getAllTag(query), HttpStatus.OK);
    }

    @GetMapping("/calendar")
    public ResponseEntity<CalendarResponse> calendarEvents(@RequestParam(required = false) Integer year) {
        if (year == null || year == 0) {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
        }
        return new ResponseEntity(postService.getCalendarPosts(year), HttpStatus.OK);
    }

    @GetMapping("/statistics/my")
    public ResponseEntity<StatisticsResponse> userStatistics(){
        return new ResponseEntity<>(userService.getUserStatistics(), HttpStatus.OK);
    }

    @GetMapping("/statistics/all")
    public ResponseEntity<StatisticsResponse> blogStatistics(){
        User currentUser = userService.getCurrentUser();
        if (currentUser.getIsModerator() == 0 && !settingsService.getGlobalSettings().isStaticIsPublic()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        SettingsResponse globalSettings = settingsService.getGlobalSettings();
        if (!globalSettings.isStaticIsPublic()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(userService.getAllStatistics(), HttpStatus.OK);
    }

    @PostMapping(value = "/image", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> uploadImage(@RequestPart("image") MultipartFile image) {

        Map<String, String> errors = checkFile(image);
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        String filename = null;
        try {
            filename = storeFile(image.getInputStream(), image.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (filename == null){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(filename, HttpStatus.OK);
    }

    @PostMapping("/comment")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> addComment(@RequestBody CommentRequest commentRequest) {

        Post post = postService.getPostById(commentRequest.getPostId());
        if(post == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        PostComment postComment = null;
        String commentStr = commentRequest.getParentId();
        if (commentStr != null && !commentStr.isEmpty()){
            int commentId = Integer.parseInt(commentStr);
            postComment = postCommentsService.getPostCommentById(commentId);
            if(postComment == null){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        Map<String, String> errors = checkComment(commentRequest.getText());
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        PostComment postCommentSave = postCommentsService.addPostComment(post, postComment, commentRequest.getText(), userService.getCurrentUser());
        return new ResponseEntity<>(new ResultResponse(postCommentSave.getId()), HttpStatus.OK);
    }

    @PostMapping("/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<ResultResponse> moderatePost(@RequestBody ModerationVotesRequest moderationRequest){
        Post post = postService.getPostById(moderationRequest.getPostId());
        if(post == null){
            return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
        }

        ModerationStatus moderationStatus = ModerationStatus.NEW;
        if (moderationRequest.getDecision().equals("decline")) {
            moderationStatus = ModerationStatus.DECLINED;
        } else if (moderationRequest.getDecision().equals("accept")) {
            moderationStatus = ModerationStatus.ACCEPTED;
        }

        post.setModerationStatus(moderationStatus);
        post.setModerator(userService.getCurrentUser());
        postService.savePost(post);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }

    @PostMapping("/profile/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> editUserProfile(@RequestBody ProfileRequest profileRequest){

        User currentUser = userService.getCurrentUser();
        Map<String, String> errors = userService.checkUserData(profileRequest, currentUser);
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.OK);
        }

        currentUser.setName(profileRequest.getName());
        currentUser.setEmail(profileRequest.getEmail());

        if (profileRequest.getPassword() != null){
            currentUser.setPassword(userService.encodePassword(profileRequest.getPassword()));
        }
        if (profileRequest.getRemovePhoto() == 1){
            currentUser.setPhoto("");
        }

        userService.saveUser(currentUser);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }

    @PostMapping(value = "/profile/my", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> editUserProfile(@ModelAttribute ProfileImageRequest profileRequest){

        User currentUser = userService.getCurrentUser();
        Map<String, String> errors = userService.checkUserData(profileRequest, currentUser);
        Map<String, String> errorsImage = checkFile(profileRequest.getPhoto());
        errors.putAll(errorsImage);
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.OK);
        }

        String filename = null;
        ResampleOp resamOp = new ResampleOp(36,36);
        try {
            BufferedImage bufferedImage = ImageIO.read(profileRequest.getPhoto().getInputStream());
            BufferedImage modifiedImage = resamOp.filter(bufferedImage, null);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(modifiedImage, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());

            filename = storeFile(is, profileRequest.getPhoto().getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (filename == null){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        currentUser.setPhoto(filename);
        currentUser.setName(profileRequest.getName());
        currentUser.setEmail(profileRequest.getEmail());

        if (profileRequest.getPassword() != null){
            currentUser.setPassword(userService.encodePassword(profileRequest.getPassword()));
        }

        userService.saveUser(currentUser);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }

    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<ResultResponse> editSettings(@RequestBody SettingsResponse settingsResponse){
        settingsService.setGlobalSettings(BlogSetting.MULTIUSER_MODE, settingsResponse.isMultiuserMode());
        settingsService.setGlobalSettings(BlogSetting.POST_PREMODERATION, settingsResponse.isPostPremoderation());
        settingsService.setGlobalSettings(BlogSetting.STATISTICS_IS_PUBLIC, settingsResponse.isStaticIsPublic());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public String storeFile(InputStream image, String originalFilename ) {
        String generatedName = newFileName();
        String uploadDir = SEPARATOR + "upload" + SEPARATOR + generatedName.substring(0,2) + SEPARATOR + generatedName.substring(2,4);
        String fileName = SEPARATOR + generatedName.substring(5) + "_"  + originalFilename;

        String realPath = request.getServletContext().getRealPath(uploadDir);
        Path fileStorageLocation = Paths.get(realPath).toAbsolutePath().normalize();

        try {
            Files.createDirectories(fileStorageLocation);
            Path targetLocation = Paths.get(fileStorageLocation + fileName);
            Files.copy(image, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return uploadDir + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, String> checkFile(MultipartFile image) {
        Map<String, String> errors = new HashMap<>();

        String contentType = image.getContentType();
        if (!contentType.equals("image/png") && !contentType.equals("image/jpeg")) {
            errors.put("image", "Формат файла не jpg или png");
        }

        long bytes = image.getSize();

        if ((bytes / 1000000) > MAX_IMAGE){
            errors.put("image", "Размер файла превышает допустимый размер");
        }
        return errors;
    }

    public static String newFileName() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private Map<String, String> checkComment(String text) {
        Map<String, String> errors = new HashMap<>();

        if (text.isEmpty() || text.length() < 3) {
            errors.put("text", "Текст комментария не задан или слишком короткий");
        }

        return errors;
    }

}
