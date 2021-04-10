package main.api.response.posts;

import lombok.Data;
import main.api.response.authorization.UserResponse;

@Data
public class PostResponse {
    private int id;
    private long timestamp;
    private UserResponse user;
    private String title;
    private String announce;
    private long likeCount;
    private long dislikeCount;
    private long commentCount;
    private long viewCount;

    public PostResponse() {
    }

    public PostResponse(int id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public PostResponse(int id, long timestamp, String title, int viewCount,
                        long likeCount, long dislikeCount, long commentCount) {
        this.id = id;
        this.timestamp = timestamp;
        this.title = title;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.commentCount = commentCount;
    }
}
