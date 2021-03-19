package main.api.response.posts;

import lombok.Data;
import main.api.response.UserResponse;


@Data
public class CommentResponse {
    private int id;
    private long timestamp;
    private String text;
    private UserResponse user;
}
