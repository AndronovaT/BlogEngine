package main.repository;

import main.model.entity.GlobalSetting;
import main.model.entity.Post;
import main.model.entity.PostComment;
import main.model.enums.BlogSetting;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GlobalSettingsRepository extends CrudRepository<GlobalSetting, Integer> {

}
