package main.api.response.posts;

import lombok.Data;
import main.api.response.UserResponse;

@Data
public class PostResponse {
    private int id;
    private long timestamp;
    private UserResponse user;
    private String title;
    private String announce;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private int viewCount;
}
