package main.controller;

import main.api.request.CommentRequest;
import main.api.request.ModerationVotesRequest;
import main.api.request.ProfileRequest;
import main.api.request.ProfileImageRequest;
import main.api.response.*;
import main.api.response.tags.AllTagsResponse;
import main.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api")
public class ApiGeneralController {

    private final InitResponse initResponse;
    private final SettingsService settingsService;
    private final TagToPostService tagToPostService;
    private final PostService postService;
    private final UserService userService;
    private final PostCommentsService postCommentsService;
    private final ImageService imageService;
    private final AccountService accountService;

    public ApiGeneralController(InitResponse initResponse, SettingsService settingsService, TagToPostService tagToPostService,
                                PostService postService, UserService userService, PostCommentsService postCommentsService,
                                ImageService imageService, AccountService accountService) {
        this.initResponse = initResponse;
        this.settingsService = settingsService;
        this.tagToPostService = tagToPostService;
        this.postService = postService;
        this.userService = userService;
        this.postCommentsService = postCommentsService;
        this.imageService = imageService;
        this.accountService = accountService;
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
        return new ResponseEntity(postService.getCalendarPosts(year), HttpStatus.OK);
    }

    @GetMapping("/statistics/my")
    public ResponseEntity<StatisticsResponse> userStatistics(){
        return new ResponseEntity<>(userService.getUserStatistics(), HttpStatus.OK);
    }

    @GetMapping("/statistics/all")
    public ResponseEntity<StatisticsResponse> blogStatistics(){
        return userService.createBlogStatisticsResponse();
    }

    @PostMapping(value = "/image", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> uploadImage(@RequestPart("image") MultipartFile image) {

        return imageService.uploadImageResponse(image);

    }

    @PostMapping("/comment")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> addComment(@RequestBody CommentRequest commentRequest) {

        return postService.addCommentResponse(commentRequest);
    }

    @PostMapping("/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<ResultResponse> moderatePost(@RequestBody ModerationVotesRequest moderationRequest){
        return postService.moderatePostResponse(moderationRequest);
    }

    @PostMapping("/profile/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> editUserProfile(@RequestBody ProfileRequest profileRequest){

        return accountService.editUserProfileResponse(profileRequest);
    }

    @PostMapping(value = "/profile/my", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> editUserProfile(@ModelAttribute ProfileImageRequest profileRequest){
        return accountService.editUserProfileWithImage(profileRequest);
    }

    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<ResultResponse> editSettings(@RequestBody SettingsResponse settingsResponse){
        settingsService.editGlobalSettings(settingsResponse);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
