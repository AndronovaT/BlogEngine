package main.repository;

import main.model.entity.TagToPost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagToPostRepository extends CrudRepository<TagToPost, Integer> {

    @Query(value = "SELECT tagPost FROM  TagToPost tagPost " +
            "INNER JOIN Tag t on tagPost.tag = t " +
            "WHERE lower(t.name) like lower(:searchTerm)")
    List<TagToPost> search(@Param("searchTerm") String searchTerm);
}
