package main.controller;

import main.api.response.posts.AllPostsResponse;
import main.api.response.posts.InfoPostResponse;
import main.model.entity.Post;
import main.model.enums.Mode;
import main.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@RestController
@RequestMapping("/api/post")
public class ApiPostController {

    private final PostService postService;

    public ApiPostController(PostService postService) {
        this.postService = postService;
    }


    @GetMapping("")
    private ResponseEntity<AllPostsResponse> allPosts(@RequestParam(defaultValue = "0") Integer offset,
                                                      @RequestParam(defaultValue = "10") Integer limit,
                                                      @RequestParam(defaultValue = "recent") Mode mode){
        return new ResponseEntity<>(postService.getAllPosts(offset, limit, mode, ""), HttpStatus.OK);
    }

    @GetMapping("/{ID}")
    private ResponseEntity<InfoPostResponse> post(@PathVariable(name = "ID") Integer id){
        Post post = postService.getPostById(id);
        if(post == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        InfoPostResponse postById = postService.getInfoPostResponse(post);

        postService.addView(post);

        return new ResponseEntity<>(postById, HttpStatus.OK);
    }

    @GetMapping("/search")
    private ResponseEntity<AllPostsResponse> searchPosts(@RequestParam(defaultValue = "0") Integer offset,
                                                         @RequestParam(defaultValue = "10") Integer limit,
                                                         @RequestParam(defaultValue = "") String query){

        return new ResponseEntity<>(postService.getAllPosts(offset, limit, Mode.recent, query), HttpStatus.OK);
    }

    @GetMapping("/byDate")
    private ResponseEntity<AllPostsResponse> getPostsByDate(@RequestParam(defaultValue = "0") Integer offset,
                                                            @RequestParam(defaultValue = "10") Integer limit,
                                                            @RequestParam(name = "date") String dateString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateString, formatter);
        return new ResponseEntity<>(postService.getPostsByDate(offset, limit, date), HttpStatus.OK);
    }

    @GetMapping("/byTag")
    private ResponseEntity<AllPostsResponse> getPostByTag(@RequestParam(defaultValue = "0") Integer offset,
                                                          @RequestParam(defaultValue = "10") Integer limit,
                                                          @RequestParam() String tag){
        return new ResponseEntity<>(postService.getPostsByTag(offset, limit, tag), HttpStatus.OK);
    }


}
