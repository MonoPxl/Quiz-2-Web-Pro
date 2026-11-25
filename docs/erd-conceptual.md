# Conceptual data model (CDM)

myITS Broadcast is a campus broadcaster. The conceptual model contains five product
entities — **User**, **Category**, **Post**, **Comment**, **Bookmark**. Event
registration and password reset are intentionally absent.

```mermaid
erDiagram
    USER ||--o{ POST : "creates"
    CATEGORY ||--o{ POST : "classifies"
    USER ||--o{ COMMENT : "writes"
    POST ||--o{ COMMENT : "contains"
    COMMENT o|--o{ COMMENT : "replies to (one level)"
    USER ||--o{ BOOKMARK : "saves"
    POST ||--o{ BOOKMARK : "is saved as"

    USER {
        int id PK
        string name
        string email UK
        string password
        bool is_moderator
        string avatar_path
        datetime created_at
        datetime updated_at
    }

    CATEGORY {
        int id PK
        string name UK
        string slug UK
        string description
    }

    POST {
        int id PK
        int user_id FK
        int category_id FK
        string type
        string status
        string title
        string slug UK
        text body
        string cover_image_path
        bool is_pinned
        datetime published_at
        datetime event_starts_at
        datetime event_ends_at
        string event_location
    }

    COMMENT {
        int id PK
        int post_id FK
        int user_id FK
        int parent_id FK
        text body
        datetime created_at
        datetime updated_at
    }

    BOOKMARK {
        int id PK
        int user_id FK
        int post_id FK
        datetime created_at
    }
```

## Relationship explanation

- **User → Post (1:N).** A user may author many announcements and events. Each post has
  exactly one author. Deleting an account cascades to its posts.
- **Category → Post (1:N).** Each post belongs to one seeded campus category. Categories
  are reference data and are not editable through the application, so deleting a category
  is restricted while posts reference it.
- **Post → Comment (1:N).** A post owns its discussion thread. Deleting a post deletes its
  comments.
- **Comment → Comment (self, 1:N).** `parent_id` is nullable. Replies are flattened to one
  level: a reply to a reply is stored under the original top-level comment. There is no
  deeper nesting capability.
- **User → Comment (1:N).** Each comment or reply has one author. Only that author may
  edit or delete it.
- **User → Bookmark (1:N) and Post → Bookmark (1:N).** A bookmark is the association
  between one user and one post. The pair is unique, so duplicate bookmarks are impossible.
- **User → Password change.** Passwords are changed from account settings after
  confirming the current password. There is no token table and no self-service reset.
- **No Event registration.** An event post is broadcast information only. Users do not
  register, RSVP, or appear on an attendee list inside myITS Broadcast.

## Scope notes

- `is_pinned` exists for data compatibility but is not surfaced in the feed or dashboard.
- `is_moderator` is preserved but grants no additional powers; the source policies are
  author-only.
- `email_verified_at` is absent. Accounts are usable immediately after registration.
- Two-factor and passkey data are absent because the source has no working user-facing
  flow for them.
