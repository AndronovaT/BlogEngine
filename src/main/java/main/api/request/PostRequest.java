package main.api.request;

import lombok.Data;

import java.util.List;

@Data
public class PostRequest {
    private long timestamp;
    private byte active;
    private String title;
    private List<String> tags;
    private String text;
}
