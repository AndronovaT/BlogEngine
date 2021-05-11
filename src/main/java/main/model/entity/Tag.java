package main.model.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
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

    public Tag() {
    }

    public Tag(@NotNull String name) {
        this.name = name;
    }
}
