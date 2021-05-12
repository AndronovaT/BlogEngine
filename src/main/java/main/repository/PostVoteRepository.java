package main.repository;

import main.model.entity.Post;
import main.model.entity.PostVote;
import main.model.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostVoteRepository extends CrudRepository<PostVote, Integer> {

    @Query(value = "SELECT vote FROM PostVote AS vote " +
            "WHERE vote.post = :post and vote.value = 1")
    List<PostVote> searchLikeByPost(@Param("post") Post post);

    @Query(value = "SELECT vote FROM PostVote AS vote " +
            "WHERE vote.post = :post and vote.value = -1")
    List<PostVote> searchDislikeByPost(@Param("post") Post post);

    @Query(value = "SELECT vote FROM PostVote AS vote " +
            "WHERE vote.post = :post and vote.user = :user")
    List<PostVote> searchLikeByUserPost(@Param("post") Post post,
                                        @Param("user") User user);

}
