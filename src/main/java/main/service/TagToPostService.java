package main.service;

import main.api.response.tags.AllTagsResponse;
import main.api.response.tags.TagResponse;
import main.model.entity.Post;
import main.model.entity.Tag;
import main.model.entity.TagToPost;
import main.persistence.PostRepository;
import main.persistence.TagToPostRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;


@Service
public class TagToPostService {

    private final TagToPostRepository tagToPostRepository;
    private final PostRepository postRepository;

    public TagToPostService(TagToPostRepository tagToPostRepository, PostRepository postRepository) {
        this.tagToPostRepository = tagToPostRepository;
        this.postRepository = postRepository;
    }

    public AllTagsResponse getAllTag(String query) {

        List<TagToPost> tagToPostList = tagToPostRepository.search(query + "%");

        if (tagToPostList == null || tagToPostList.size() == 0){
            return new AllTagsResponse();
        }

        List<Post> posts = postRepository.search(Sort.by(Sort.Direction.DESC, "time"), "%");
        int sizePosts = 0;

        if (posts != null) {
            sizePosts = posts.size();
        }

        Map<Tag, Long> countTagMap = tagToPostList.stream()
                .collect(groupingBy(TagToPost::getTag, Collectors.counting()));

        Map<Tag, Float> tagWeight = new HashMap<>();

        for(Map.Entry<Tag, Long> item : countTagMap.entrySet()){
            Long value = item.getValue();
            float w = (sizePosts == 0 ? 0F : ((float) value) / sizePosts);
            tagWeight.put(item.getKey(), w);
        }

        Float maxWeigth = tagWeight.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                .get()
                .getValue();
        Float k = 1 / maxWeigth;

        List<TagResponse> tagResponseList = new ArrayList<>();
        for(Map.Entry<Tag, Float> item : tagWeight.entrySet()){
            TagResponse tagResponse = new TagResponse();
            tagResponse.setName(item.getKey().getName());

            float n = k * item.getValue() * 100;
            int round = Math.round(n);
            float result = (float) round / 100;
            tagResponse.setWeight(result);

            tagResponseList.add(tagResponse);
        }

        AllTagsResponse allTagsResponse = new AllTagsResponse();
        allTagsResponse.setTags(tagResponseList);

        return allTagsResponse;
    }
}
