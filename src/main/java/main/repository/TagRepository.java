package main.repository;

import main.model.entity.Post;
import main.model.entity.Tag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends CrudRepository<Tag, Integer> {

    @Query(value = "SELECT t FROM TagToPost tagPost " +
            "INNER JOIN Tag t on tagPost.tag = t " +
            "WHERE tagPost.post = :post")
    List<Tag> tagByPost(@Param("post") Post post);
}
