package id.myitsbroadcast.model;

import id.myitsbroadcast.config.AppConfig;

import java.time.LocalDateTime;

public class Post {

    private long id;
    private long userId;
    private long categoryId;
    private PostType type = PostType.ANNOUNCEMENT;
    private PostStatus status = PostStatus.DRAFT;
    private String title;
    private String slug;
    private String body;
    private String coverImagePath;
    private boolean pinned;
    private LocalDateTime publishedAt;
    private LocalDateTime eventStartsAt;
    private LocalDateTime eventEndsAt;
    private String eventLocation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User author;
    private Category category;
    private long commentCount;
    private long bookmarkCount;
    private boolean bookmarked;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public PostType getType() {
        return type;
    }

    public void setType(PostType type) {
        this.type = type;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getEventStartsAt() {
        return eventStartsAt;
    }

    public void setEventStartsAt(LocalDateTime eventStartsAt) {
        this.eventStartsAt = eventStartsAt;
    }

    public LocalDateTime getEventEndsAt() {
        return eventEndsAt;
    }

    public void setEventEndsAt(LocalDateTime eventEndsAt) {
        this.eventEndsAt = eventEndsAt;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public long getBookmarkCount() {
        return bookmarkCount;
    }

    public void setBookmarkCount(long bookmarkCount) {
        this.bookmarkCount = bookmarkCount;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public boolean isEvent() {
        return type == PostType.EVENT;
    }

    /** Mirrors the source's "published and the publication time has passed" rule. */
    public boolean isPublished() {
        return status == PostStatus.PUBLISHED
                && publishedAt != null
                && !publishedAt.isAfter(LocalDateTime.now(java.time.ZoneId.of(AppConfig.APP_TIMEZONE)));
    }

    public boolean isDraft() {
        return status == PostStatus.DRAFT;
    }

    /** Plain-text preview used by feed cards. */
    public String getExcerpt() {
        if (body == null) {
            return "";
        }

        String plain = body.replaceAll("\\s+", " ").trim();

        return plain.length() <= 155 ? plain : plain.substring(0, 152).trim() + "...";
    }

    public boolean isOwnedBy(User user) {
        return user != null && user.getId() == userId;
    }
}
