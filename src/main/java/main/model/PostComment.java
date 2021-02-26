package main.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "post_comments")
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PostComment getParentPostComment() {
        return parentPostComment;
    }

    public void setParentPostComment(PostComment parentPostComment) {
        this.parentPostComment = parentPostComment;
    }

    public List<PostComment> getChildPostComments() {
        return childPostComments;
    }

    public void setChildPostComments(List<PostComment> childPostComments) {
        this.childPostComments = childPostComments;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
