package main.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "post_comments")
@NoArgsConstructor
public class PostComment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private PostComment parentPostComment;

    @OneToMany(mappedBy = "parentPostComment")
    private List<PostComment> childPostComments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @NotNull
    private Date time;

    @NotNull
    @Column(columnDefinition = "TEXT")
    @Type(type = "text")
    private String text;

    public PostComment(User user, Post post, @NotNull String text) {
        this.user = user;
        this.post = post;
        this.text = text;
    }
}
