package main.api.response.tags;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AllTagsResponse {

    private List<TagResponse> tags = new ArrayList<>();

}
