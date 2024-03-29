package main.model.entity;

import lombok.Data;
import main.model.enums.Role;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
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

    public User() {
    }

    public User(@NotNull String name,  @NotNull String password, @NotNull String email) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.regTime = new Date();
    }

    public Role getRole() {
        return isModerator == 1 ? Role.MODERATOR : Role.USER;
    }
}
