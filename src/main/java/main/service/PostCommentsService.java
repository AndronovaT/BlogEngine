package main.service;

import main.model.entity.Post;
import main.model.entity.PostComment;
import main.repository.PostCommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostCommentsService {
    private final PostCommentRepository postCommentRepository;

    public PostCommentsService(PostCommentRepository postCommentRepository) {
        this.postCommentRepository = postCommentRepository;
    }

    public List<PostComment> searchByPost(Post post){
        return postCommentRepository.searchByPost(post);
    }
}
