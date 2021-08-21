package main.service;

import main.model.entity.Post;
import main.model.entity.PostVote;
import main.model.entity.User;
import main.repository.PostVoteRepository;
import org.springframework.stereotype.Service;


import java.util.Date;
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

    public boolean addVote(Post post, User currentUser, byte vote) {
        List<PostVote> postVotes = searchByPostUser(post, currentUser);
        PostVote postVote = new PostVote();

        if (!postVotes.isEmpty()){
            postVote = postVotes.get(0);
            if (postVote.getValue() == vote) {
                return false;
            } else {
                postVote.setValue(vote);
            }
        } else {
            postVote.setValue(vote);
            postVote.setPost(post);
            postVote.setUser(currentUser);
            postVote.setTime(new Date());
        }
        save(postVote);
        return true;
    }


}
