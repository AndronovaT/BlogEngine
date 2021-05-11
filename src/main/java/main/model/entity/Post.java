package main.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import main.api.response.authorization.UserResponse;
import main.model.enums.ModerationStatus;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "posts")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    @Column(name = "is_active")
    @JsonIgnore
    private byte isActive;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", columnDefinition="enum('NEW','ACCEPTED','DECLINED')")
    @JsonIgnore
    private ModerationStatus moderationStatus = ModerationStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    @JsonIgnore
    private User moderator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @NotNull
    @JsonIgnore
    private Date time;

    @NotNull
    private String title;

    @NotNull
    @Column(columnDefinition = "TEXT")
    @Type(type = "text")
    @JsonIgnore
    private String text;

    @NotNull
    @Column(name = "view_count")
    private int viewCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<PostVote> postVotes;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TagToPost> tagToPosts;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<PostComment> postComments;

    @Transient
    private long timestamp;

    @Transient
    @JsonProperty("user")
    private UserResponse userResponse;

    @Transient
    private String announce;

    @Transient
    private long likeCount;

    @Transient
    private long dislikeCount;

    @Transient
    private long commentCount;

    public Post() {
    }

    public Post(int id, long timestamp, User user, String title, String announce, int viewCount,
                        long likeCount, long dislikeCount, long commentCount) {
        this.id = id;
        this.timestamp = timestamp;
        this.user = user;
        this.title = title;
        this.announce = announce.replaceAll("<[^>]+>", "");
        if (this.announce.length() > 150) {
            this.announce = this.announce.substring(0, 150) + "...";
        }
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.commentCount = commentCount;
    }
}
