package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ModerationVotesRequest {
    @JsonProperty("post_id")
    private int postId;
    private String decision;
}
