package id.myitsbroadcast.service;

import id.myitsbroadcast.dao.BookmarkDao;
import id.myitsbroadcast.model.Bookmark;
import id.myitsbroadcast.model.Post;
import id.myitsbroadcast.model.User;

import java.util.List;

public class BookmarkService {

    private final BookmarkDao bookmarks = new BookmarkDao();

    /** Add or remove the bookmark and return the resulting state. */
    public boolean toggle(User user, Post post) {
        if (bookmarks.exists(user.getId(), post.getId())) {
            bookmarks.remove(user.getId(), post.getId());

            return false;
        }

        bookmarks.add(user.getId(), post.getId());

        return true;
    }

    public List<Bookmark> listFor(User user) {
        return bookmarks.listForUser(user.getId());
    }

    public boolean isBookmarked(User user, Post post) {
        return user != null && bookmarks.exists(user.getId(), post.getId());
    }
}
