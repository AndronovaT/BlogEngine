package main.controller;

import main.api.request.CommentRequest;
import main.api.request.ModerationRequest;
import main.api.response.*;
import main.api.response.tags.AllTagsResponse;
import main.model.entity.Post;
import main.model.entity.PostComment;
import main.model.enums.ModerationStatus;
import main.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
    private static final long MAX_IMAGE = 50;
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
    public ResponseEntity<MyStatisticsResponse> userStatistics(){
        return new ResponseEntity<>(userService.getUserStatistics(), HttpStatus.OK);
    }

    @PostMapping(value = "/image", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> uploadImage(@RequestPart("image") MultipartFile image) {

        Map<String, String> errors = checkFile(image);
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        String filename = storeFile(image);
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
    public ResponseEntity<ResultResponse> moderatePost(@RequestBody ModerationRequest moderationRequest){
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

    public String storeFile(MultipartFile image) {
        String generatedName = newFileName();
        String pathFile = "upload" + SEPARATOR + generatedName.substring(0,2) + SEPARATOR + generatedName.substring(2,4);
        String fileName = SEPARATOR + generatedName.substring(5) + "_" +image.getOriginalFilename();

        Path fileStorageLocation = Paths.get(pathFile).toAbsolutePath().normalize();

        try {
            Files.createDirectories(fileStorageLocation);
            Path targetLocation = Paths.get(fileStorageLocation.toString() + fileName);
            Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return SEPARATOR + pathFile + fileName;
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
