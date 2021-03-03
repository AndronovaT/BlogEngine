package main.service;

import main.api.response.posts.AllPostsResponse;
import org.springframework.stereotype.Service;

@Service
public class AllPostsService {

    public AllPostsResponse getAllPosts(){
        return new AllPostsResponse();
    }
}
