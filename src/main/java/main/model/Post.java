package main.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte getIsActive() {
        return isActive;
    }

    public void setIsActive(byte isActive) {
        this.isActive = isActive;
    }

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(ModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public User getModerator() {
        return moderator;
    }

    public void setModerator(User moderator) {
        this.moderator = moderator;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public List<PostVote> getPostVotes() {
        return postVotes;
    }

    public void setPostVotes(List<PostVote> postVotes) {
        this.postVotes = postVotes;
    }

    public List<TagToPost> getTagToPosts() {
        return tagToPosts;
    }

    public void setTagToPosts(List<TagToPost> tagToPosts) {
        this.tagToPosts = tagToPosts;
    }

    public List<PostComment> getPostComments() {
        return postComments;
    }

    public void setPostComments(List<PostComment> postComments) {
        this.postComments = postComments;
    }
}
