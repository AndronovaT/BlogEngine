package main.api.response.posts;

import lombok.Data;
import main.api.response.authorization.UserResponse;

import java.util.List;

@Data
public class InfoPostResponse {

    private int id;

    private long timestamp;

    private UserResponse user;

    private String title;

    private String text;

    private int likeCount;

    private int dislikeCount;

    private int commentCount;

    private int viewCount;

    private boolean active;

    private List<CommentResponse> comments;

    private List<String> tags;
}
