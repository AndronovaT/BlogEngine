package main.persistence;

import main.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface PostRepository extends PagingAndSortingRepository<Post, Integer> {

    @Query(value = "SELECT p FROM Post p " +
            "WHERE p.isActive = 1 " +
            "and p.moderationStatus = 'ACCEPTED' " +
            "and p.time <= NOW() ")
    List<Post> search();

    @Query(value = "SELECT p FROM Post p " +
            "WHERE p.isActive = 1 " +
            "and p.moderationStatus = 'ACCEPTED' " +
            "and p.time <= NOW() " +
            "and lower(p.text) LIKE lower(:query) ")
    Page<Post> search(Pageable page, @Param("query") String query);

    @Query(value = "SELECT p FROM Post p " +
            "WHERE p.isActive = 1 " +
            "and p.moderationStatus = 'ACCEPTED' " +
            "and DATE(p.time) = :datePost")
    Page<Post> searchByDate(Pageable page, @Param("datePost") Date date);

    @Query(value = "SELECT p FROM Post p " +
            "INNER JOIN TagToPost tag_p ON p = tag_p.post " +
            "INNER JOIN Tag t ON tag_p.tag = t " +
            "WHERE p.isActive = 1 " +
            "and p.moderationStatus = 'ACCEPTED' " +
            "and t.name = :tag")
    Page<Post> searchByTag(Pageable page, @Param("tag") String tag);

}
