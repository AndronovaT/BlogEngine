package main.api.response.posts;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PostResponse {
    private int id;
    private long timestamp;
    private Map<Integer, String> user = new HashMap<>();
    private String title;
    private String announce;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private int viewCount;
}
