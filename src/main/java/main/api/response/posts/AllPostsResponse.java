package main.api.response.posts;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AllPostsResponse {
    private int count;
    private List<PostResponse> posts = new ArrayList<>();
}
