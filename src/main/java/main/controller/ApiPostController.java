package main.controller;

import main.api.response.posts.AllPostsResponse;
import main.service.AllPostsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/post")
public class ApiPostController {

    private final AllPostsService allPostsService;

    public ApiPostController(AllPostsService allPostsService) {
        this.allPostsService = allPostsService;
    }


    @GetMapping("/{offset}{limit}{mode}")
    private ResponseEntity<AllPostsResponse> allPosts(@PathVariable(name = "offset") String offset,
                                                      @PathVariable(name = "limit") String limit,
                                                      @PathVariable(name = "mode") String mode){
        System.out.println("Hello");
        return new ResponseEntity<>(allPostsService.getAllPosts(), HttpStatus.OK);
    }

}
