package main.controller;

import main.api.response.posts.AllPostsResponse;
import main.api.response.posts.InfoPostResponse;
import main.model.entity.Post;
import main.model.enums.Mode;
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
  //  @PreAuthorize("hasAuthority('user:write')")
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
 //   @PreAuthorize("hasAuthority('user:moderate')")
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


}
