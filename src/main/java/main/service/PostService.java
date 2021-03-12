package main.service;

import main.api.response.CalendarResponse;
import main.api.response.posts.AllPostsResponse;
import main.api.response.posts.PostResponse;
import main.model.entity.Post;
import main.model.entity.User;
import main.model.enums.Mode;
import main.persistence.PostRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PostService {

    private final PostRepository postRepository;
    @PersistenceContext
    EntityManager em;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public AllPostsResponse getAllPosts(int offset, int limit, Mode mode, String query){

        List<Post> posts = postRepository.search(Sort.by((mode == Mode.early ? Sort.Direction.ASC : Sort.Direction.DESC)
                                                        ,"time"), "%" + query + "%");

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

    public CalendarResponse getCalendarPosts(Integer year){

        List<Tuple> resultList = getResultQueryCalendarCount(em);

        if (resultList.isEmpty()) {
            return new CalendarResponse();
        }

        CalendarResponse calendarResponse = new CalendarResponse();
        Map<String, Integer> posts = new HashMap<>();
        Set<Integer> years = new HashSet<>();
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");

        Calendar c = Calendar.getInstance();  
        c.set(year, 1, 1);

        resultList.forEach(res -> {
            years.add((Integer) res.get("year"));

            Date timePost = (Date) res.get("time");
            if (timePost.after(c.getTime())) {
                Long countPosts = (Long) res.get("countPosts");
                Integer count = Math.toIntExact(countPosts);
                posts.put(formatDate.format(res.get("time")), count);
            }
        });

        calendarResponse.setYears(years);
        calendarResponse.setPosts(posts);

        return calendarResponse;
    }

    private List<Tuple> getResultQueryCalendarCount(EntityManager em) {
        return em
               .createQuery( "SELECT YEAR(p.time) as year, p.time as time, COUNT(p.id) as countPosts " +
                        "FROM Post p " +
                        "WHERE p.isActive = 1 " +
                        "and p.moderationStatus = 'ACCEPTED' " +
                        "and p.time <= NOW()" +
                        "GROUP BY YEAR(p.time), p.time", Tuple.class)
               .getResultList();
    }

}
