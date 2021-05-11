package main.repository;

import main.model.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    @Query(value = "SELECT user FROM User user " +
            "WHERE lower(user.email) LIKE lower(:email) ")
    List<User> findByEmail(@Param("email") String email);

    @Query(value = "SELECT user FROM User user " +
            "WHERE user.code LIKE :code ")
    List<User> findByCode(@Param("code") String code);

}
