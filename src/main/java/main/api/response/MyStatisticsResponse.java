package main.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class MyStatisticsResponse {
    private long postsCount;
    private int likesCount;
    private int dislikesCount;
    private long viewsCount;
    private long firstPublication;

    public MyStatisticsResponse() {
    }

}
