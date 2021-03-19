package main.service;

import main.model.entity.Post;
import main.model.entity.Tag;
import main.persistence.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> tagByPost(Post post) {
        return tagRepository.tagByPost(post);
    }
}
