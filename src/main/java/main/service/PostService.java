package main.service;

import main.api.response.CalendarResponse;
import main.api.response.authorization.UserResponse;
import main.api.response.posts.AllPostsResponse;
import main.api.response.posts.CommentResponse;
import main.api.response.posts.InfoPostResponse;
import main.api.response.posts.PostResponse;
import main.model.entity.*;
import main.model.enums.Mode;
import main.model.enums.ModerationStatus;
import main.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import java.time.LocalDate;
import java.util.*;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostVoteService postVoteService;
    private final PostCommentsService postCommentsService;
    private final TagService tagService;

    @PersistenceContext
    EntityManager em;

    public PostService(PostRepository postRepository, PostVoteService postVoteService,
                       PostCommentsService postCommentsService, TagService tagService) {
        this.postRepository = postRepository;
        this.postVoteService = postVoteService;
        this.postCommentsService = postCommentsService;
        this.tagService = tagService;
    }

    public AllPostsResponse getAllPosts(int offset, int limit, Mode mode, String query){

        Pageable page = PageRequest.of(offset, limit, (mode.getSort()));

        Page<Object[]> postsPage = postRepository.search(page, "%" + query + "%");

        return getAllPostsResponse(postsPage);
    }

    public void addView(Post post){

        if (post.getIsActive() == 1 && post.getModerationStatus() == ModerationStatus.ACCEPTED) {
            int count = post.getViewCount();
            count++;
            post.setViewCount(count);

            postRepository.save(post);
        }
    }

    public Post getPostById(int id){
        Optional<Post> byId = postRepository.findById(id);
        if (!byId.isPresent()) {
            return null;
        }
        return byId.get();
    }

    public InfoPostResponse getInfoPostResponse(Post post) {
        InfoPostResponse infoPostResponse = new InfoPostResponse();
        infoPostResponse.setId(post.getId());

        long seconds = getSeconds(post.getTime());
        infoPostResponse.setTimestamp(seconds);

        User userPost = post.getUser();
        UserResponse user = new UserResponse();
        user.setId(userPost.getId());
        user.setName(userPost.getName());
        infoPostResponse.setUser(user);

        infoPostResponse.setTitle(post.getTitle());
        infoPostResponse.setText(post.getText());

        infoPostResponse.setLikeCount(getLike(post));
        infoPostResponse.setDislikeCount(getDislike(post));

        infoPostResponse.setCommentCount(getComments(post));
        infoPostResponse.setViewCount(post.getViewCount());

        if (post.getIsActive() == 1 && post.getModerationStatus() == ModerationStatus.ACCEPTED) {
            infoPostResponse.setActive(true);
        } else {
            infoPostResponse.setActive(false);
        }

        List<CommentResponse> commentResponseList = new ArrayList<>();
        List<PostComment> commentList = postCommentsService.searchByPost(post);
        commentList.forEach(postComment -> {
            CommentResponse commentResponse = getCommentResponse(postComment);
            commentResponseList.add(commentResponse);
        });
        infoPostResponse.setComments(commentResponseList);

        List<String> nameTags = new ArrayList<>();
        List<Tag> tags = tagService.tagByPost(post);
        tags.forEach(tag -> {
            nameTags.add(tag.getName());
        });
        infoPostResponse.setTags(nameTags);

        return infoPostResponse;
    }

    private CommentResponse getCommentResponse(PostComment postComment) {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(postComment.getId());

        long sec = getSeconds(postComment.getTime());
        commentResponse.setTimestamp(sec);

        commentResponse.setText(postComment.getText());

        User userComment = postComment.getUser();
        UserResponse userCommentResponse = new UserResponse();
        userCommentResponse.setId(userComment.getId());
        userCommentResponse.setName(userComment.getName());
        userCommentResponse.setPhoto(userComment.getPhoto());
        commentResponse.setUser(userCommentResponse);
        return commentResponse;
    }

    public AllPostsResponse getPostsByDate(int offset, int limit, LocalDate date){

        java.sql.Date dateSQl = java.sql.Date.valueOf(date);
        Mode mode = Mode.recent;

        Pageable page = PageRequest.of(offset, limit, (mode.getSort()));
        Page<Object[]> postsPage = postRepository.searchByDate(page, dateSQl);

        return getAllPostsResponse(postsPage);
    }

    public AllPostsResponse getPostsByTag(int offset, int limit, String tag){

        Mode mode = Mode.recent;
        Pageable page = PageRequest.of(offset, limit, (mode.getSort()));
        Page<Object[]> postsPage = postRepository.searchByTag(page, tag);

        return getAllPostsResponse(postsPage);
    }

    private AllPostsResponse getAllPostsResponse(Page<Object[]> postsPage) {
        if (postsPage == null || !postsPage.hasContent()) {
            AllPostsResponse emptyPostsResponse = new AllPostsResponse();
            emptyPostsResponse.setCount(0);
            return emptyPostsResponse;
        }

        List<Object[]> resultList = postsPage.getContent();

        AllPostsResponse allPostsResponse = new AllPostsResponse();
        allPostsResponse.setCount(Math.toIntExact(postsPage.getTotalElements()));
        List<PostResponse> postResponseList = getPostListResponses(resultList);
        allPostsResponse.setPosts(postResponseList);
        return allPostsResponse;
    }

    private List<PostResponse> getPostListResponses(List<Object[]> resultList) {
        List<PostResponse> postResponseList = new ArrayList<>();

        resultList.forEach(res -> {
            PostResponse postResponse = new PostResponse((Integer) res[0], (Long) res[1], (String) res[3], (Integer) res[5],
                    (Long) res[6], (Long) res[7], (Long) res[8] );

            User userPost = (User) res[2];
            UserResponse user = new UserResponse();
            user.setId(userPost.getId());
            user.setName(userPost.getName());
            postResponse.setUser(user);

            String text = (String) res[4];
            postResponse.setAnnounce(text.replaceAll("\\<.*?\\>", "").substring(0, 150) + "...");

            postResponseList.add(postResponse);
        });

        return postResponseList;
    }

    private int getComments(Post post) {
        int comments = 0;
        List<PostComment> commentList = postCommentsService.searchByPost(post);
        if (commentList != null) {
            comments = commentList.size();
        }
        return comments;
    }

    private int getDislike(Post post) {
        int dislike = 0;
        List<PostVote> dislikes =  postVoteService.searchDislikeByPost(post);
        if (dislikes != null) {
            dislike = dislikes.size();
        }
        return dislike;
    }

    private int getLike(Post post) {
        int like = 0;
        List<PostVote> likes = postVoteService.searchLikeByPost(post);
        if (likes != null) {
            like = likes.size();
        }
        return like;
    }

    private long getSeconds(Date time) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(time);
        return calendar.getTimeInMillis() / 1000L;
    }

    public CalendarResponse getCalendarPosts(Integer year){

        CalendarResponse calendarResponse = new CalendarResponse(year, getResultQueryCalendarCount());
        return calendarResponse;
    }

    private List<Tuple> getResultQueryCalendarCount() {
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
