package main.api.response.posts;

import lombok.Data;
import main.model.entity.Post;

import java.util.ArrayList;
import java.util.List;

@Data
public class AllPostsResponse {

    private int count;

    private List<Post> posts = new ArrayList<>();

}
