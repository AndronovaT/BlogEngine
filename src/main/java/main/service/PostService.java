package main.service;

import main.api.response.posts.AllPostsResponse;
import main.api.response.posts.PostResponse;
import main.model.entity.Post;
import main.model.entity.User;
import main.model.enums.Mode;
import main.persistence.PostRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public AllPostsResponse getAllPosts(int offset, int limit, Mode mode){

        List<Post> posts = postRepository.search(Sort.by((mode == Mode.early ? Sort.Direction.ASC : Sort.Direction.DESC)
                                                        ,"time"));

        if (posts == null || posts.size() == 0) {
            AllPostsResponse emptyPostsResponse = new AllPostsResponse();
            emptyPostsResponse.setCount(0);
            return emptyPostsResponse;
        }

        AllPostsResponse allPostsResponse = new AllPostsResponse();
        allPostsResponse.setCount(posts.size());
        List<PostResponse> postResponseList = getPostResponses(offset, limit, posts);

        if (mode == Mode.popular){
            postResponseList.stream().sorted(Comparator.comparing(PostResponse::getCommentCount));
        } else if (mode == Mode.best) {
            postResponseList.stream().sorted(Comparator.comparing(PostResponse::getLikeCount));
        }

        allPostsResponse.setPosts(postResponseList);

        return allPostsResponse;
    }

    private List<PostResponse> getPostResponses(int offset, int limit, List<Post> posts) {
        List<PostResponse> postResponseList = new ArrayList<>();
        int shift = offset * limit;

        for (int i = shift; i < shift + limit && i < posts.size(); i++){
            Post post = posts.get(i);
            PostResponse postResponse = new PostResponse();
            postResponse.setId(post.getId());

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTime(post.getTime());
            long seconds = calendar.getTimeInMillis() / 1000L;
            postResponse.setTimestamp(seconds);

            Map<Integer, String> userMap = new HashMap<>();
            User userPost = post.getUser();
            userMap.put(userPost.getId(), userPost.getName());
            postResponse.setUser(userMap);

            postResponse.setTitle(post.getTitle());
            postResponse.setAnnounce(post.getText().replaceAll("\\<.*?\\>", "").substring(0, 150) + "...");

            int like = (int) post.getPostVotes().stream().filter(postVote -> postVote.getValue() == 1).count();
            int dislike = (int) post.getPostVotes().stream().filter(postVote -> postVote.getValue() == -1).count();
            postResponse.setLikeCount(like);
            postResponse.setDislikeCount(dislike);

            postResponse.setCommentCount(post.getPostComments() == null ? 0 :post.getPostComments().size());
            postResponse.setViewCount(post.getViewCount());

            postResponseList.add(postResponse);
        }
        return postResponseList;
    }

}
