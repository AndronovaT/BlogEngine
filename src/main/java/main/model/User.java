package main.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @NotNull
    @Column(name = "is_moderator")
    private byte isModerator;
    @NotNull
    @Column(name = "reg_time")
    private Date regTime;
    @NotNull
    private String name;
    @NotNull
    private String email;
    @NotNull
    private String password;
    private String code;
    @Column(columnDefinition = "TEXT")
    @Type(type = "text")
    private String photo;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> postsUser;
    @OneToMany(mappedBy = "moderator", cascade = CascadeType.ALL)
    private List<Post> postsModerator;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PostVote> postVotes;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PostComment> postComments;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte getIsModerator() {
        return isModerator;
    }

    public void setIsModerator(byte isModerator) {
        this.isModerator = isModerator;
    }

    public Date getRegTime() {
        return regTime;
    }

    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<Post> getPostsUser() {
        return postsUser;
    }

    public void setPostsUser(List<Post> postsUser) {
        this.postsUser = postsUser;
    }

    public List<Post> getPostsModerator() {
        return postsModerator;
    }

    public void setPostsModerator(List<Post> postsModerator) {
        this.postsModerator = postsModerator;
    }

    public List<PostVote> getPostVotes() {
        return postVotes;
    }

    public void setPostVotes(List<PostVote> postVotes) {
        this.postVotes = postVotes;
    }

    public List<PostComment> getPostComments() {
        return postComments;
    }

    public void setPostComments(List<PostComment> postComments) {
        this.postComments = postComments;
    }
}
