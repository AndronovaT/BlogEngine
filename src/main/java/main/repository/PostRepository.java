package main.repository;

import main.model.entity.Post;
import main.model.entity.User;
import main.model.enums.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends PagingAndSortingRepository<Post, Integer>{

    Optional<Post> getPostById(Integer id);

    @Query(value = "SELECT p FROM Post p " +
            "WHERE p.isActive = 1 " +
            "and p.moderationStatus = 'ACCEPTED' " +
            "and p.time <= NOW() ")
    List<Post> searchAllPost();

    @Query(value = "SELECT new main.model.entity.Post(" +
            "p.id AS id, " +
            "UNIX_TIMESTAMP(p.time) AS timestamp, " +
            "MAX(p.user) AS user, " +
            "MAX(p.title) AS title, " +
            "MAX(p.text) AS announce, " +
            "MAX(p.viewCount) AS viewCount, " +
            "SUM(CASE WHEN pVote.value = 1 THEN 1 ELSE 0 END) AS likeCount, " +
            "SUM(CASE WHEN pVote.value = -1 THEN 1 ELSE 0 END) AS dislikeCount, " +
            "COUNT(DISTINCT pComment) AS commentCount) " +
            "FROM Post p " +
            "LEFT JOIN PostComment pComment ON pComment.post = p " +
            "LEFT JOIN PostVote pVote ON pVote.post = p " +
            "WHERE p.isActive = 1 " +
            "and p.moderationStatus = 'ACCEPTED' " +
            "and p.time <= NOW() " +
            "and lower(p.text) LIKE lower(:query) " +
            "GROUP BY p.id, UNIX_TIMESTAMP(p.time)")
    Page<Post> search(Pageable page, @Param("query") String query);

    @Query(value = "SELECT new main.model.entity.Post( " +
            "p.id AS id, " +
            "UNIX_TIMESTAMP(p.time) AS timestamp, " +
            "MAX(p.user) AS user, " +
            "MAX(p.title) AS title, " +
            "MAX(p.text) AS announce, " +
            "MAX(p.viewCount) AS viewCount, " +
            "SUM(CASE WHEN pVote.value = 1 THEN 1 ELSE 0 END) AS likeCount, " +
            "SUM(CASE WHEN pVote.value = -1 THEN 1 ELSE 0 END) AS dislikeCount, " +
            "SUM(CASE WHEN pComment IS NOT NULL THEN 1 ELSE 0 END) AS commentCount) " +
            "FROM Post p " +
            "LEFT JOIN PostComment pComment ON pComment.post = p " +
            "LEFT JOIN PostVote pVote ON pVote.post = p " +
            "WHERE p.isActive = 1 " +
            "and p.moderationStatus = 'ACCEPTED' " +
            "and DATE(p.time) = :datePost " +
            "GROUP BY p.id, UNIX_TIMESTAMP(p.time)")
    Page<Post> searchByDate(Pageable page, @Param("datePost") Date date);

    @Query(value = "SELECT new main.model.entity.Post( " +
            "p.id AS id, " +
            "UNIX_TIMESTAMP(p.time) AS timestamp, " +
            "MAX(p.user) AS user, " +
            "MAX(p.title) AS title, " +
            "MAX(p.text) AS announce, " +
            "MAX(p.viewCount) AS viewCount, " +
            "SUM(CASE WHEN pVote.value = 1 THEN 1 ELSE 0 END) AS likeCount, " +
            "SUM(CASE WHEN pVote.value = -1 THEN 1 ELSE 0 END) AS dislikeCount, " +
            "SUM(CASE WHEN pComment IS NOT NULL THEN 1 ELSE 0 END) AS commentCount) " +
            "FROM Post p " +
            "INNER JOIN TagToPost tag_p ON p = tag_p.post " +
            "INNER JOIN Tag t ON tag_p.tag = t " +
            "LEFT JOIN PostComment pComment ON pComment.post = p " +
            "LEFT JOIN PostVote pVote ON pVote.post = p " +
            "WHERE p.isActive = 1 " +
            "and p.moderationStatus = 'ACCEPTED' " +
            "and t.name = :tag " +
            "GROUP BY p.id, UNIX_TIMESTAMP(p.time)")
    Page<Post> searchByTag(Pageable page, @Param("tag") String tag);

    @Query(value = "SELECT new main.model.entity.Post( " +
            "p.id AS id, " +
            "UNIX_TIMESTAMP(p.time) AS timestamp, " +
            "MAX(p.user) AS user, " +
            "MAX(p.title) AS title, " +
            "MAX(p.text) AS announce, " +
            "MAX(p.viewCount) AS viewCount, " +
            "SUM(CASE WHEN pVote.value = 1 THEN 1 ELSE 0 END) AS likeCount, " +
            "SUM(CASE WHEN pVote.value = -1 THEN 1 ELSE 0 END) AS dislikeCount, " +
            "SUM(CASE WHEN pComment IS NOT NULL THEN 1 ELSE 0 END) AS commentCount) " +
            "FROM Post p " +
            "LEFT JOIN PostComment pComment ON pComment.post = p " +
            "LEFT JOIN PostVote pVote ON pVote.post = p " +
            "WHERE p.user = :user AND " +
            "p.isActive = :isActive AND " +
            "p.moderationStatus = :moderationStatus " +
            "GROUP BY p.id, UNIX_TIMESTAMP(p.time)")
    Page<Post> searchByCurrentUser(Pageable page,
                                           @Param("user") User user,
                                           @Param("isActive") byte isActive,
                                           @Param("moderationStatus") ModerationStatus moderationStatus);


    @Query(value = "SELECT new main.model.entity.Post( " +
            "p.id AS id, " +
            "UNIX_TIMESTAMP(p.time) AS timestamp, " +
            "MAX(p.user) AS user, " +
            "MAX(p.title) AS title, " +
            "MAX(p.text) AS announce, " +
            "MAX(p.viewCount) AS viewCount, " +
            "SUM(CASE WHEN pVote.value = 1 THEN 1 ELSE 0 END) AS likeCount, " +
            "SUM(CASE WHEN pVote.value = -1 THEN 1 ELSE 0 END) AS dislikeCount, " +
            "SUM(CASE WHEN pComment IS NOT NULL THEN 1 ELSE 0 END) AS commentCount) " +
            "FROM Post p " +
            "LEFT JOIN PostComment pComment ON pComment.post = p " +
            "LEFT JOIN PostVote pVote ON pVote.post = p " +
            "WHERE p.moderator = :moderator AND " +
            "p.isActive = 1 AND " +
            "p.moderationStatus = :moderationStatus " +
            "GROUP BY p.id, UNIX_TIMESTAMP(p.time)")
    Page<Post> searchByModerator(Pageable page,
                                           @Param("moderator") User moderator,
                                           @Param("moderationStatus") ModerationStatus moderationStatus);

    @Query(value = "SELECT new main.model.entity.Post( " +
            "p.id AS id, " +
            "UNIX_TIMESTAMP(p.time) AS timestamp, " +
            "MAX(p.user) AS user, " +
            "MAX(p.title) AS title, " +
            "MAX(p.text) AS announce, " +
            "MAX(p.viewCount) AS viewCount, " +
            "SUM(CASE WHEN pVote.value = 1 THEN 1 ELSE 0 END) AS likeCount, " +
            "SUM(CASE WHEN pVote.value = -1 THEN 1 ELSE 0 END) AS dislikeCount, " +
            "SUM(CASE WHEN pComment IS NOT NULL THEN 1 ELSE 0 END) AS commentCount) " +
            "FROM Post p " +
            "LEFT JOIN PostComment pComment ON pComment.post = p " +
            "LEFT JOIN PostVote pVote ON pVote.post = p " +
            "WHERE p.isActive =  1 AND " +
            "p.moderationStatus = 'NEW' " +
            "GROUP BY p.id, UNIX_TIMESTAMP(p.time)")
    Page<Post> searchForModeration(Pageable page);

}
