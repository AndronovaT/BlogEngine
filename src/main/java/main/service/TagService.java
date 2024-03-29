package main.service;

import main.model.entity.Post;
import main.model.entity.Tag;
import main.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> tagByPost(Post post, String tagText) {
        return tagRepository.tagByPost(post, tagText);
    }

    public List<Tag> searchByName(String name) {
        return tagRepository.search(name);
    }

    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }
}
