package main.persistence;

import main.model.entity.Post;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Integer> {

    @Query(value = "SELECT p FROM Post p " +
            "WHERE p.isActive = 1 " +
            "and p.moderationStatus = 'ACCEPTED' " +
            "and p.time <= NOW() " +
            "and lower(p.text) LIKE lower(:query) ")
    List<Post> search(Sort sort, @Param("query") String query);

}
