package main.controller;

import main.api.request.ModerationVotesRequest;
import main.api.request.PostRequest;
import main.api.response.ResultResponse;
import main.api.response.posts.AllPostsResponse;
import main.api.response.posts.InfoPostResponse;
import main.model.enums.Mode;
import main.model.enums.ModerationStatus;
import main.model.enums.StatusMyPosts;
import main.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;



@RestController
@RequestMapping("/api/post")
public class ApiPostController {

    private final PostService postService;
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ApiPostController(PostService postService) {
        this.postService = postService;
    }


    @GetMapping("")
    public ResponseEntity<AllPostsResponse> allPosts(@RequestParam(defaultValue = "0") Integer offset,
                                                      @RequestParam(defaultValue = "10") Integer limit,
                                                      @RequestParam(defaultValue = "recent") Mode mode){
        return new ResponseEntity<>(postService.getAllPosts(offset, limit, mode, ""), HttpStatus.OK);
    }

    @GetMapping("/{ID}")
    public ResponseEntity<InfoPostResponse> post(@PathVariable(name = "ID") Integer id){
        return postService.getInfoPostResponse(id);
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

        return postService.addPostResponse(postRequest);
    }


    @PutMapping("/{ID}")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> editPost(@PathVariable(name = "ID") Integer id,
                                                   @RequestBody PostRequest postRequest){
        return postService.editPostResponse(id, postRequest);
    }


    @PostMapping("/like")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> like(@RequestBody ModerationVotesRequest votesRequest){

        return postService.getLikeResponse(votesRequest, (byte) 1);

    }


    @PostMapping("/dislike")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> dislike(@RequestBody ModerationVotesRequest votesRequest){

        return postService.getLikeResponse(votesRequest, (byte) -1);

    }
}
