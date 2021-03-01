package main.model.entity;

import lombok.Data;
import main.model.enums.ModerationStatus;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @NotNull
    @Column(name = "is_active")
    private byte isActive;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", columnDefinition="enum('NEW','ACCEPTED','DECLINED')")
    private ModerationStatus moderationStatus = ModerationStatus.NEW;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private User moderator;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @NotNull
    private Date time;
    @NotNull
    private String title;
    @NotNull
    @Column(columnDefinition = "TEXT")
    @Type(type = "text")
    private String text;
    @NotNull
    @Column(name = "view_count")
    private int viewCount;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostVote> postVotes;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<TagToPost> tagToPosts;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostComment> postComments;
}
