package main.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @NotNull
    private String name;
    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL)
    private List<TagToPost> tagToPosts;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TagToPost> getTagToPosts() {
        return tagToPosts;
    }

    public void setTagToPosts(List<TagToPost> tagToPosts) {
        this.tagToPosts = tagToPosts;
    }
}
