package id.myitsbroadcast.service;

import id.myitsbroadcast.dao.BookmarkDao;
import id.myitsbroadcast.dao.PostDao;
import id.myitsbroadcast.model.Bookmark;
import id.myitsbroadcast.model.Post;
import id.myitsbroadcast.model.PostStatus;
import id.myitsbroadcast.model.User;

import java.util.List;

/**
 * Dashboard figures are always computed from the user's real rows; nothing is
 * cached or invented.
 */
public class DashboardService {

    private final PostDao posts = new PostDao();
    private final BookmarkDao bookmarks = new BookmarkDao();

    public static class Dashboard {
        private final List<Post> posts;
        private final List<Bookmark> bookmarks;
        private final long postCount;
        private final long publishedCount;
        private final long draftCount;
        private final long bookmarkCount;

        Dashboard(List<Post> posts, List<Bookmark> bookmarks, long postCount,
                  long publishedCount, long draftCount, long bookmarkCount) {
            this.posts = posts;
            this.bookmarks = bookmarks;
            this.postCount = postCount;
            this.publishedCount = publishedCount;
            this.draftCount = draftCount;
            this.bookmarkCount = bookmarkCount;
        }

        public List<Post> getPosts() {
            return posts;
        }

        public List<Bookmark> getBookmarks() {
            return bookmarks;
        }

        public long getPostCount() {
            return postCount;
        }

        public long getPublishedCount() {
            return publishedCount;
        }

        public long getDraftCount() {
            return draftCount;
        }

        public long getBookmarkCount() {
            return bookmarkCount;
        }
    }

    public Dashboard forUser(User user) {
        List<Post> ownPosts = posts.latestForUser(user.getId());
        List<Bookmark> ownBookmarks = bookmarks.listForUser(user.getId());

        long published = ownPosts.stream()
                .filter(post -> post.getStatus() == PostStatus.PUBLISHED)
                .count();
        long drafts = ownPosts.stream()
                .filter(post -> post.getStatus() == PostStatus.DRAFT)
                .count();

        return new Dashboard(ownPosts, ownBookmarks, ownPosts.size(), published, drafts, ownBookmarks.size());
    }
}
