package id.myitsbroadcast.model;

import java.util.List;

/** A single page of results plus the metadata needed to render pagination. */
public class Page<T> {

    private final List<T> items;
    private final long total;
    private final int page;
    private final int perPage;

    public Page(List<T> items, long total, int page, int perPage) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.perPage = perPage;
    }

    public List<T> getItems() {
        return items;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getPerPage() {
        return perPage;
    }

    public int getTotalPages() {
        return perPage <= 0 ? 1 : Math.max(1, (int) Math.ceil((double) total / perPage));
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }

    public int getPreviousPage() {
        return Math.max(1, page - 1);
    }

    public int getNextPage() {
        return Math.min(getTotalPages(), page + 1);
    }

    public long getFrom() {
        return total == 0 ? 0 : (long) (page - 1) * perPage + 1;
    }

    public long getTo() {
        return Math.min(total, (long) page * perPage);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
