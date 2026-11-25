package id.myitsbroadcast.web.forms;

/** Raw strings from the create/edit form; validation lives in PostService. */
public class PostForm {

    private String title = "";
    private String categoryId = "";
    private String type = "announcement";
    private String body = "";
    private String status = "";
    private String eventStartsAt = "";
    private String eventEndsAt = "";
    private String eventLocation = "";
    private boolean removeCoverImage;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId == null ? "" : categoryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? "announcement" : type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body == null ? "" : body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? "" : status;
    }

    public String getEventStartsAt() {
        return eventStartsAt;
    }

    public void setEventStartsAt(String eventStartsAt) {
        this.eventStartsAt = eventStartsAt == null ? "" : eventStartsAt;
    }

    public String getEventEndsAt() {
        return eventEndsAt;
    }

    public void setEventEndsAt(String eventEndsAt) {
        this.eventEndsAt = eventEndsAt == null ? "" : eventEndsAt;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation == null ? "" : eventLocation;
    }

    public boolean isRemoveCoverImage() {
        return removeCoverImage;
    }

    public void setRemoveCoverImage(boolean removeCoverImage) {
        this.removeCoverImage = removeCoverImage;
    }

    public boolean isEvent() {
        return "event".equals(type);
    }
}
