package main.service;

import main.api.request.CommentRequest;
import main.api.request.ModerationVotesRequest;
import main.api.request.PostRequest;
import main.api.response.CalendarResponse;
import main.api.response.ResultResponse;
import main.api.response.authorization.UserResponse;
import main.api.response.posts.AllPostsResponse;
import main.api.response.posts.CommentResponse;
import main.api.response.posts.InfoPostResponse;
import main.model.entity.*;
import main.model.enums.Mode;
import main.model.enums.ModerationStatus;
import main.model.enums.StatusMyPosts;
import main.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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
    private final TagToPostService tagToPostService;
    private final UserService userService;
    private final SettingsService settingsService;

    @PersistenceContext
    EntityManager em;

    public PostService(PostRepository postRepository, PostVoteService postVoteService,
                       PostCommentsService postCommentsService, TagService tagService,
                       TagToPostService tagToPostService, UserService userService, SettingsService settingsService) {
        this.postRepository = postRepository;
        this.postVoteService = postVoteService;
        this.postCommentsService = postCommentsService;
        this.tagService = tagService;
        this.tagToPostService = tagToPostService;
        this.userService = userService;
        this.settingsService = settingsService;
    }

    public AllPostsResponse getAllPosts(int offset, int limit, Mode mode, String query){

        Pageable page = PageRequest.of(offset, limit, (mode.getSort()));

        Page<Post> postsPage = postRepository.search(page, "%" + query + "%");

        return getAllPostsResponse(postsPage);

    }

    public ResponseEntity<InfoPostResponse> getInfoPostResponse(@PathVariable(name = "ID") Integer id) {

        Post post = getPostById(id);
        if(post == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        InfoPostResponse postById = getInfoPostResponse(post);

        addView(post);

        return new ResponseEntity<>(postById, HttpStatus.OK);
    }

    public void addView(Post post){

        User currentUser = userService.getCurrentUser();
        if (currentUser.getIsModerator() == 0 && !currentUser.equals(post.getUser())) {
            if (post.getIsActive() == 1 && post.getModerationStatus() == ModerationStatus.ACCEPTED) {
                int count = post.getViewCount();
                count++;
                post.setViewCount(count);

                postRepository.save(post);
            }
        }
    }

    public void savePost(Post post){
        postRepository.save(post);
    }

    public Post getPostById(int id){
        Optional<Post> byId = postRepository.findById(id);
        return byId.orElse(null);
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
        List<Tag> tags = tagService.tagByPost(post, "%%");
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
        Page<Post> postsPage = postRepository.searchByDate(page, dateSQl);

        return getAllPostsResponse(postsPage);

    }

    public AllPostsResponse getPostsByTag(int offset, int limit, String tag){

        Mode mode = Mode.recent;
        Pageable page = PageRequest.of(offset, limit, (mode.getSort()));
        Page<Post> postsPage = postRepository.searchByTag(page, tag);

        return getAllPostsResponse(postsPage);
    }

    private AllPostsResponse getAllPostsResponse(Page<Post> postsPage) {

        if (postsPage == null || !postsPage.hasContent()) {
            AllPostsResponse emptyPostsResponse = new AllPostsResponse();
            emptyPostsResponse.setCount(0);
            return emptyPostsResponse;
        }

        List<Post> postList = postsPage.getContent();

        AllPostsResponse allPostsResponse = new AllPostsResponse();
        allPostsResponse.setCount(Math.toIntExact(postsPage.getTotalElements()));

        postList.forEach(post -> {
            UserResponse user = new UserResponse();
            user.setId(post.getUser().getId());
            user.setName(post.getUser().getName());
            post.setUserResponse(user);
        });

        allPostsResponse.setPosts(postList);
        return allPostsResponse;

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

        if (year == null || year == 0) {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
        }

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

    public AllPostsResponse getAllPostModerator(Integer offset, Integer limit, ModerationStatus moderationStatus) {
        User moderator = userService.getCurrentUser();
        Pageable page = PageRequest.of(offset, limit);

        Page<Post> postsPage;
        if(moderationStatus == ModerationStatus.NEW) {
            postsPage = postRepository.searchForModeration(page);
        } else {
            postsPage = postRepository.searchByModerator(page, moderator, moderationStatus);
        }
        return getAllPostsResponse(postsPage);
    }

    public AllPostsResponse getAllPostCurrentUser(int offset, int limit, StatusMyPosts status) {
        User currentUser = userService.getCurrentUser();

        Pageable page = PageRequest.of(offset, limit);

        byte isActive = 1;
        ModerationStatus moderationStatus = ModerationStatus.NEW;
        if (status == StatusMyPosts.inactive){
            isActive = 0;
        }

        if (status == StatusMyPosts.declined){
            moderationStatus = ModerationStatus.DECLINED;
        }

        if (status == StatusMyPosts.published){
            moderationStatus = ModerationStatus.ACCEPTED;
        }

        Page<Post> postsPage = postRepository.searchByCurrentUser(page, currentUser, isActive, moderationStatus);
        return getAllPostsResponse(postsPage);
    }

    public Post addPost(PostRequest postRequest){
        Post post = new Post();
        post.setIsActive(postRequest.getActive());

        ModerationStatus moderationStatus = getModerationStatus();

        post.setModerationStatus(moderationStatus);
        post.setText(postRequest.getText());
        post.setTitle(postRequest.getTitle());
        long currentSec = getSeconds(new Date());
        post.setTime( currentSec > postRequest.getTimestamp() ? new Date() : new Date(postRequest.getTimestamp()));
        post.setUser(userService.getCurrentUser());

        Post newPost = postRepository.save(post);

        addTagToNewPost(postRequest, newPost, false);

        return newPost;
    }

    private ModerationStatus getModerationStatus() {
        ModerationStatus moderationStatus;
        if (settingsService.getGlobalSettings().isPostPremoderation()
                && userService.getCurrentUser().getIsModerator() == 0) {
            moderationStatus = ModerationStatus.NEW;
        } else {
            moderationStatus = ModerationStatus.ACCEPTED;
        }
        return moderationStatus;
    }

    private void addTagToNewPost(PostRequest postRequest, Post newPost, boolean update) {
        List<String> tags = postRequest.getTags();

        for (String tagText: tags) {
            if (update) {
                List<Tag> tagsPost = tagService.tagByPost(newPost, tagText);
                if (!tagsPost.isEmpty()) {
                    continue;
                }
            }

            List<Tag> tagsByName = tagService.searchByName(tagText);
            Tag currentTag;

            if (!tagsByName.isEmpty()) {
                currentTag = tagsByName.get(0);
            } else {
                Tag tag = new Tag(tagText);
                currentTag = tagService.save(tag);
            }

            TagToPost tagToPost = new TagToPost(newPost, currentTag);
            tagToPostService.save(tagToPost);

        }
    }

    public Post editPost(Post post, PostRequest postRequest){
        post.setIsActive(postRequest.getActive());
        post.setText(postRequest.getText());
        post.setTitle(postRequest.getTitle());
        long currentSec = getSeconds(new Date());
        post.setTime( currentSec > postRequest.getTimestamp() ? new Date() : new Date(postRequest.getTimestamp()));

        User currentUser =  userService.getCurrentUser();
        if (currentUser.equals(post.getUser())) {
            post.setModerationStatus(ModerationStatus.NEW);
        }
        Post newPost = postRepository.save(post);
        addTagToNewPost(postRequest, newPost, true);

        return newPost;
    }

    public ResponseEntity<ResultResponse> moderatePostResponse(@RequestBody ModerationVotesRequest moderationRequest) {
        Post post = getPostById(moderationRequest.getPostId());
        if(post == null){
            return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);
        }

        ModerationStatus moderationStatus = ModerationStatus.NEW;
        if (moderationRequest.getDecision().equals("decline")) {
            moderationStatus = ModerationStatus.DECLINED;
        } else if (moderationRequest.getDecision().equals("accept")) {
            moderationStatus = ModerationStatus.ACCEPTED;
        }

        post.setModerationStatus(moderationStatus);
        post.setModerator(userService.getCurrentUser());
        savePost(post);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }

    public ResponseEntity<ResultResponse> addPostResponse(@RequestBody PostRequest postRequest) {
        Map<String, String> errors = checkPost(postRequest);
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.OK);
        }

        addPost(postRequest);

        return new ResponseEntity(new ResultResponse(true), HttpStatus.OK);
    }

    public ResponseEntity<ResultResponse> editPostResponse(@PathVariable(name = "ID") Integer id, @RequestBody PostRequest postRequest) {
        Post post = getPostById(id);
        if(post == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, String> errors = checkPost(postRequest);
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.OK);
        }

        editPost(post, postRequest);

        return new ResponseEntity(new ResultResponse(true), HttpStatus.OK);
    }

    public ResponseEntity<ResultResponse> getLikeResponse(@RequestBody ModerationVotesRequest votesRequest, byte i) {
        Post post = getPostById(votesRequest.getPostId());
        if (post == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User currentUser = userService.getCurrentUser();

        byte like = i;
        if (!postVoteService.addVote(post, currentUser, like)) return new ResponseEntity<>(new ResultResponse(false), HttpStatus.OK);

        return new ResponseEntity<>(new ResultResponse(true), HttpStatus.OK);
    }

    private Map<String, String> checkPost(PostRequest postRequest) {
        Map<String, String> errors = new HashMap<>();

        if (postRequest.getTitle() == null || postRequest.getTitle().equals("")){
            errors.put("title", "Заголовок не установлен");
        }

        if (postRequest.getTitle().length() < 3){
            errors.put("title", "Заголовок слишком короткий");
        }

        if (postRequest.getText() == null || postRequest.getText().equals("")){
            errors.put("text", "Текст публикации не установлен");
        }

        if (postRequest.getText().length() < 3){
            errors.put("text", "Текст публикации слишком короткий");
        }

        return errors;
    }

    public ResponseEntity<ResultResponse> addCommentResponse(@RequestBody CommentRequest commentRequest) {

        Post post = getPostById(commentRequest.getPostId());
        if(post == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        PostComment postComment = null;
        String commentStr = commentRequest.getParentId();
        if (commentStr != null && !commentStr.isEmpty()){
            int commentId = Integer.parseInt(commentStr);
            postComment = postCommentsService.getPostCommentById(commentId);
            if(postComment == null){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        Map<String, String> errors = checkComment(commentRequest.getText());
        if (errors.size() > 0){
            return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        PostComment postCommentSave = postCommentsService.addPostComment(post, postComment, commentRequest.getText(), userService.getCurrentUser());
        return new ResponseEntity<>(new ResultResponse(postCommentSave.getId()), HttpStatus.OK);
    }

    private Map<String, String> checkComment(String text) {
        Map<String, String> errors = new HashMap<>();

        if (text.isEmpty() || text.length() < 3) {
            errors.put("text", "Текст комментария не задан или слишком короткий");
        }

        return errors;
    }

}
