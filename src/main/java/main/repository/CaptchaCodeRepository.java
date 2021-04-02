package main.repository;

import main.model.entity.CaptchaCode;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaptchaCodeRepository extends CrudRepository<CaptchaCode, Integer> {

    @Query(value = "SELECT code FROM CaptchaCode code " +
            "WHERE code.secretCode LIKE (:secret) ")
    List<CaptchaCode> findBySecret(@Param("secret") String secret);

    @Query(value = "SELECT code FROM CaptchaCode code " +
            "WHERE TIMESTAMPDIFF(MINUTE, code.time, NOW()) > :minutes ")
    List<CaptchaCode> findOldCaptcha(@Param("minutes") Integer minutes);
}
