package main.api.response.posts;

import lombok.Data;
import main.api.response.authorization.UserResponse;


@Data
public class CommentResponse {
    private int id;
    private long timestamp;
    private String text;
    private UserResponse user;
}
