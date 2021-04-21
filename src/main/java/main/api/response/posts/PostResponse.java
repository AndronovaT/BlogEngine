package main.api.response.posts;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import main.api.response.authorization.UserResponse;
import main.model.entity.User;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponse {
    private int id;
    private long timestamp;
    private UserResponse user;
    private User userPost;
    private String title;
    private String announce;
    private long likeCount;
    private long dislikeCount;
    private long commentCount;
    private long viewCount;

    public PostResponse() {
    }

    public PostResponse(int id, long timestamp, User userPost, String title, String announce, int viewCount,
                        long likeCount, long dislikeCount, long commentCount) {
        this.id = id;
        this.timestamp = timestamp;
        this.userPost = userPost;
        this.title = title;
        this.announce = announce.replaceAll("\\<.*?\\>", "");
        if (this.announce.length() > 150) {
            this.announce = announce.substring(0, 150) + "...";
        }
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.commentCount = commentCount;
    }
}
