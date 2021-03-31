package main.repository;

import main.model.entity.Post;
import main.model.entity.PostComment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends CrudRepository<PostComment, Integer> {

    @Query(value = "SELECT comment FROM PostComment AS comment " +
            "WHERE comment.post = :post")
    List<PostComment> searchByPost(@Param("post") Post post);

}
