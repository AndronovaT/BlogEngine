package main.controller;

import main.api.request.ModerationVotesRequest;
import main.api.request.PostRequest;
import main.api.response.ResultResponse;
import main.api.response.posts.AllPostsResponse;
import main.api.response.posts.InfoPostResponse;
import main.model.entity.Post;
import main.model.entity.PostVote;
import main.model.entity.User;
import main.model.enums.Mode;
import main.model.enums.ModerationStatus;
import main.model.enums.StatusMyPosts;
import main.service.PostService;
import main.service.PostVoteService;
import main.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/post")
public class ApiPostController {

    private final PostService postService;
    private final UserService userService;
    private final PostVoteService postVoteService;
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ApiPostController(PostService postService, UserService userService, PostVoteService postVoteService) {
        this.postService = postService;
        this.userService = userService;
        this.postVoteService = postVoteService;
    }


    @GetMapping("")
    public ResponseEntity<AllPostsResponse> allPosts(@RequestParam(defaultValue = "0") Integer offset,
                                                      @RequestParam(defaultValue = "10") Integer limit,
                                                      @RequestParam(defaultValue = "recent") Mode mode){
        return new ResponseEntity<>(postService.getAllPosts(offset, limit, mode, ""), HttpStatus.OK);
    }

    @GetMapping("/{ID}")
    public ResponseEntity<InfoPostResponse> post(@PathVariable(name = "ID") Integer id){
        Post post = postService.getPostById(id);
        if(post == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        InfoPostResponse postById = postService.getInfoPostResponse(post);

        postService.addView(post);

        return new ResponseEntity<>(postById, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<AllPostsResponse> searchPosts(@RequestParam(defaultValue = "0") Integer offset,
                                                         @RequestParam(defaultValue = "10") Integer limit,
                                                         @RequestParam(defaultValue = "") String query){
        return new ResponseEntity<>(postService.getAllPosts(offset, limit, Mode.recent, query), HttpStatus.OK);
    }

    @GetMapping("/byDate")
    public ResponseEntity<AllPostsResponse> getPostsByDate(@RequestParam(defaultValue = "0") Integer offset,
                                                            @RequestParam(defaultValue = "10") Integer limit,
                                                            @RequestParam(name = "date") String dateString){
        LocalDate date = LocalDate.parse(dateString, FORMATTER);
        return new ResponseEntity<>(postService.getPostsByDate(offset, limit, date), HttpStatus.OK);
    }

    @GetMapping("/byTag")
    public ResponseEntity<AllPostsResponse> getPostByTag(@RequestParam(defaultValue = "0") Integer offset,
                                                          @RequestParam(defaultValue = "10") Integer limit,
                                                          @RequestParam() String tag){
        return new ResponseEntity<>(postService.getPostsByTag(offset, limit, tag), HttpStatus.OK);
    }

    @GetMapping("/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<AllPostsResponse> getPostByModerator(@RequestParam(defaultValue = "0") Integer offset,
                                                               @RequestParam(defaultValue = "10") Integer limit,
                                                               @RequestParam String status){
        ModerationStatus moderationStatus = ModerationStatus.NEW;
        if (status.equals("declined")) {
            moderationStatus = ModerationStatus.DECLINED;
        } else if (status.equals("accepted")){
            moderationStatus = ModerationStatus.ACCEPTED;
        }
        return new ResponseEntity<>(postService.getAllPostModerator(offset, limit, moderationStatus), HttpStatus.OK);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AllPostsResponse> getPostByUser(@RequestParam(defaultValue = "0") Integer offset,
                                                          @RequestParam(defaultValue = "10") Integer limit,
                                                          @RequestParam StatusMyPosts status) {
        return new ResponseEntity<>(postService.getAllPostCurrentUser(offset, limit, status), HttpStatus.OK);
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> addPost(@RequestBody PostRequest postRequest){

        Map<String, String> errors = checkPost(postRequest);
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.OK);
        }

        postService.addPost(postRequest);
        
        return new ResponseEntity(new ResultResponse(true), HttpStatus.OK);
    }

    private Map<String, String> checkPost(PostRequest postRequest) {
        Map<String, String> errors = new HashMap<>();

        if (postRequest.getTitle() == null || postRequest.getTitle().equals("")){
            errors.put("title", "Заголовок не установлен");
        }

        if (postRequest.getTitle().length() < 3){
            errors.put("title", "Заголовок слишком короткий");
        }

        if (postRequest.getText() == null || postRequest.getText().equals("")){
            errors.put("text", "Текст публикации не установлен");
        }

        if (postRequest.getText().length() < 3){
            errors.put("text", "Текст публикации слишком короткий");
        }

        return errors;
    }

    @PutMapping("/{ID}")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> editPost(@PathVariable(name = "ID") Integer id,
                                                   @RequestBody PostRequest postRequest){
        Post post = postService.getPostById(id);
        if(post == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, String> errors = checkPost(postRequest);
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.OK);
        }

        postService.editPost(post, postRequest);

        return new ResponseEntity(new ResultResponse(true), HttpStatus.OK);
    }


    @PostMapping("/like")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> like(@RequestBody ModerationVotesRequest votesRequest){

        Post post = postService.getPostById(votesRequest.getPostId());
        if(post == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User currentUser = userService.getCurrentUser();

        byte like = 1;
        if (!addVote(post, currentUser, like)) return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }

    @PostMapping("/dislike")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> dislike(@RequestBody ModerationVotesRequest votesRequest){

        Post post = postService.getPostById(votesRequest.getPostId());
        if(post == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User currentUser = userService.getCurrentUser();

        byte dislike = -1;
        if (!addVote(post, currentUser, dislike)) return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }

    private boolean addVote(Post post, User currentUser, byte vote) {
        List<PostVote> postVotes = postVoteService.searchByPostUser(post, currentUser);
        PostVote postVote = new PostVote();

        if (!postVotes.isEmpty()){
            postVote = postVotes.get(0);
            if (postVote.getValue() == vote) {
                return false;
            } else {
                postVote.setValue(vote);
            }
        } else {
            postVote.setValue(vote);
            postVote.setPost(post);
            postVote.setUser(currentUser);
            postVote.setTime(new Date());
        }
        postVoteService.save(postVote);
        return true;
    }

}
