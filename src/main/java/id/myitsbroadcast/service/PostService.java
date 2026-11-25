package id.myitsbroadcast.service;

import id.myitsbroadcast.dao.BookmarkDao;
import id.myitsbroadcast.dao.CategoryDao;
import id.myitsbroadcast.dao.CommentDao;
import id.myitsbroadcast.dao.PostDao;
import id.myitsbroadcast.model.Category;
import id.myitsbroadcast.model.Page;
import id.myitsbroadcast.model.Post;
import id.myitsbroadcast.model.PostStatus;
import id.myitsbroadcast.model.PostType;
import id.myitsbroadcast.model.User;
import id.myitsbroadcast.util.Errors;
import id.myitsbroadcast.util.Slug;
import id.myitsbroadcast.util.Uploads;
import id.myitsbroadcast.web.forms.PostForm;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PostService {

    public static final int PER_PAGE = 9;

    private static final DateTimeFormatter INPUT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm");

    private final PostDao posts = new PostDao();
    private final CategoryDao categories = new CategoryDao();
    private final CommentDao comments = new CommentDao();

    public Page<Post> feed(String keyword, String categorySlug, String typeValue, int page, User viewer) {
        PostType type = PostType.fromValue(typeValue);

        return posts.publishedPage(keyword, categorySlug, type, page, PER_PAGE,
                viewer == null ? null : viewer.getId());
    }

    public List<Category> categories() {
        return categories.all();
    }

    public Category categoryBySlug(String slug) {
        return categories.findBySlug(slug);
    }

    public PostType typeFrom(String value) {
        return PostType.fromValue(value);
    }

    /**
     * Load a post only when the viewer may see it: published posts are public,
     * drafts are visible to their author alone.
     */
    public Post findVisible(String slug, User viewer) {
        Post post = posts.findBySlug(slug, viewer == null ? null : viewer.getId());

        if (post == null) {
            return null;
        }

        boolean visible = post.isPublished() || (viewer != null && post.getUserId() == viewer.getId());

        return visible ? post : null;
    }

    public boolean canEdit(User user, Post post) {
        return user != null && post != null && post.getUserId() == user.getId();
    }

    public List<Post> postsBy(User user) {
        return posts.latestForUser(user.getId());
    }

    public long countComments(long postId) {
        return comments.countForPost(postId);
    }

    public Errors validate(PostForm form, Part coverImage) {
        Errors errors = new Errors();

        String title = form.getTitle().trim();
        if (title.isEmpty()) {
            errors.add("title", "The title field is required.");
        } else if (title.length() > 180) {
            errors.add("title", "The title must not be longer than 180 characters.");
        }

        try {
            long categoryId = Long.parseLong(form.getCategoryId());
            if (!categories.exists(categoryId)) {
                errors.add("categoryId", "The selected category is invalid.");
            }
        } catch (NumberFormatException exception) {
            errors.add("categoryId", "The category field is required.");
        }

        if (PostType.fromValue(form.getType()) == null) {
            errors.add("type", "The selected post type is invalid.");
        }

        String body = form.getBody().trim();
        if (body.isEmpty()) {
            errors.add("body", "The body field is required.");
        } else if (body.length() > 20_000) {
            errors.add("body", "The body must not be longer than 20,000 characters.");
        }

        PostStatus status = PostStatus.fromValue(form.getStatus());
        if (status != PostStatus.DRAFT && status != PostStatus.PUBLISHED) {
            errors.add("status", "Choose whether to save a draft or publish the broadcast.");
        }

        if (form.isEvent()) {
            LocalDateTime starts = parseDateTime(form.getEventStartsAt());
            LocalDateTime ends = parseDateTime(form.getEventEndsAt());

            if (form.getEventStartsAt().isBlank()) {
                errors.add("eventStartsAt", "The event start is required for event posts.");
            } else if (starts == null) {
                errors.add("eventStartsAt", "The event start must be a valid date and time.");
            }

            if (form.getEventEndsAt().isBlank()) {
                errors.add("eventEndsAt", "The event end is required for event posts.");
            } else if (ends == null) {
                errors.add("eventEndsAt", "The event end must be a valid date and time.");
            }

            if (starts != null && ends != null && ends.isBefore(starts)) {
                errors.add("eventEndsAt", "The event end must be after or equal to the event start.");
            }

            if (form.getEventLocation().isBlank()) {
                errors.add("eventLocation", "The event location is required for event posts.");
            } else if (form.getEventLocation().trim().length() > 255) {
                errors.add("eventLocation", "The event location must not be longer than 255 characters.");
            }
        }

        try {
            Uploads.validate(coverImage);
        } catch (Uploads.InvalidUploadException exception) {
            errors.add("coverImage", exception.getMessage());
        }

        return errors;
    }

    public Post create(User author, PostForm form, Part coverImage)
            throws Uploads.InvalidUploadException, IOException {
        Post post = new Post();
        post.setUserId(author.getId());
        applyForm(post, form);
        post.setSlug(Slug.unique(post.getTitle(), slug -> posts.slugExists(slug, null)));

        if (post.getStatus() == PostStatus.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now(java.time.ZoneId.of(
                    id.myitsbroadcast.config.AppConfig.APP_TIMEZONE)));
        }

        String stored = Uploads.store(coverImage, "posts/covers");

        if (stored != null) {
            post.setCoverImagePath(stored);
        }

        long id = posts.create(post);
        post.setId(id);

        return post;
    }

    public Post update(Post post, PostForm form, Part coverImage)
            throws Uploads.InvalidUploadException, IOException {
        String previousCover = post.getCoverImagePath();
        String previousTitle = post.getTitle();

        applyForm(post, form);

        if (!post.getTitle().equals(previousTitle)) {
            post.setSlug(Slug.unique(post.getTitle(), slug -> posts.slugExists(slug, post.getId())));
        }

        if (form.isRemoveCoverImage() && post.getCoverImagePath() != null) {
            Uploads.delete(post.getCoverImagePath());
            post.setCoverImagePath(null);
            previousCover = null;
        }

        String stored = Uploads.store(coverImage, "posts/covers");

        if (stored != null) {
            if (previousCover != null) {
                Uploads.delete(previousCover);
            }

            post.setCoverImagePath(stored);
        }

        PostStatus status = PostStatus.fromValue(form.getStatus());

        if (status == PostStatus.PUBLISHED && !post.isPublished()) {
            post.setPublishedAt(LocalDateTime.now(java.time.ZoneId.of(
                    id.myitsbroadcast.config.AppConfig.APP_TIMEZONE)));
        } else if (status == PostStatus.DRAFT) {
            post.setPublishedAt(null);
        }

        posts.update(post);

        return post;
    }

    public void delete(Post post) {
        String cover = post.getCoverImagePath();
        posts.delete(post.getId());
        Uploads.delete(cover);
    }

    public Post reload(Post post) {
        return posts.findById(post.getId());
    }

    public Post findById(long id) {
        return posts.findById(id);
    }

    private void applyForm(Post post, PostForm form) {
        post.setCategoryId(Long.parseLong(form.getCategoryId()));
        post.setType(PostType.fromValue(form.getType()));
        post.setStatus(PostStatus.fromValue(form.getStatus()));
        post.setTitle(form.getTitle().trim());
        post.setBody(form.getBody().trim());

        boolean event = post.getType() == PostType.EVENT;
        post.setEventStartsAt(event ? parseDateTime(form.getEventStartsAt()) : null);
        post.setEventEndsAt(event ? parseDateTime(form.getEventEndsAt()) : null);
        post.setEventLocation(event ? form.getEventLocation().trim() : null);
    }

    public static LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value.trim(), INPUT);
        } catch (DateTimeParseException ignored) {
            // Fall through to lenient formats used by tests and legacy data.
        }

        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ignored) {
            // Fall through to a date-only value with a neutral time.
        }

        try {
            return LocalDate.parse(value.trim()).atTime(LocalTime.NOON);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
