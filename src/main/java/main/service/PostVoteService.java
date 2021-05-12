package main.service;

import main.model.entity.Post;
import main.model.entity.PostVote;
import main.model.entity.User;
import main.repository.PostVoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostVoteService {

    private final PostVoteRepository postVoteRepository;

    public PostVoteService(PostVoteRepository postVoteRepository) {
        this.postVoteRepository = postVoteRepository;
    }

    public List<PostVote> searchDislikeByPost(Post post){
        List<PostVote> dislikes =  postVoteRepository.searchDislikeByPost(post);
        return dislikes;
    }

    public List<PostVote> searchLikeByPost(Post post){
        List<PostVote> likes =  postVoteRepository.searchLikeByPost(post);
        return likes;
    }

    public List<PostVote> searchByPostUser(Post post, User user){
        return postVoteRepository.searchLikeByUserPost(post, user);
    }

    public PostVote save (PostVote postVote) {
       return postVoteRepository.save(postVote);
    }
}
