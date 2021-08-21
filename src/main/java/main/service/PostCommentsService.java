package main.service;

import main.model.entity.Post;
import main.model.entity.PostComment;
import main.model.entity.User;
import main.repository.PostCommentRepository;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class PostCommentsService {

    private final PostCommentRepository postCommentRepository;

    public PostCommentsService(PostCommentRepository postCommentRepository) {
        this.postCommentRepository = postCommentRepository;
    }

    public List<PostComment> searchByPost(Post post){
        return postCommentRepository.searchByPost(post);
    }

    public PostComment getPostCommentById(int id){
        Optional<PostComment> byId = postCommentRepository.findById(id);
        return byId.orElse(null);
    }

    public PostComment addPostComment(Post post, PostComment parentComment, String text, User user){
        PostComment postComment = new PostComment();
        postComment.setParentPostComment(parentComment);
        postComment.setPost(post);
        postComment.setUser(user);
        postComment.setText(text);
        postComment.setTime(new Date());
        PostComment save = postCommentRepository.save(postComment);
        return save;
    }



}
