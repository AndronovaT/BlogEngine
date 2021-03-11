package main.controller;

import main.api.response.posts.AllPostsResponse;
import main.model.enums.Mode;
import main.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.CriteriaBuilder;


@RestController
@RequestMapping("/api/post")
public class ApiPostController {

    private final PostService postService;

    public ApiPostController(PostService postService) {
        this.postService = postService;
    }


    @GetMapping("")
    private ResponseEntity<AllPostsResponse> allPosts(@RequestParam(required = false, defaultValue = "0", name = "offset") Integer offset,
                                                      @RequestParam(required = false, defaultValue = "10", name = "limit") Integer limit,
                                                      @RequestParam(required = false, defaultValue = "recent", name = "mode") Mode mode){
        return new ResponseEntity<>(postService.getAllPosts(offset, limit, mode), HttpStatus.OK);
    }

}
