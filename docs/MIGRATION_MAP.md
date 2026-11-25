# Migration map — Laravel to Jakarta JSP

This document records how the existing Laravel application was translated into the
Jakarta EE 10 JSP application. It is a behavioral map, not a claim that the database
import has been executed. The Laravel source and `database/database.sqlite` remain in
the repository, untouched, as the recoverable reference.

---

## 1. Runtime and architecture

| Laravel | JSP application |
|---|---|
| PHP 8.3 / Laravel 13 | Java 17 / Jakarta EE 10 |
| Blade + Livewire + Tailwind/Vite | JSP 3.1 + JSTL 3.0 + hand-written CSS/JS |
| Eloquent models | JavaBeans/DTOs (`model/`) |
| Controllers / Form Requests | Servlets (`web/`) + service validation (`service/`) |
| Policies | Authorization checks in services/servlets |
| Middleware (`auth`, `verified`, `password.confirm`) | `AuthFilter`, `ConfirmPasswordServlet`, CSRF filter |
| SQLite | PostgreSQL via JDBC + HikariCP |
| Laravel migrations | `src/main/resources/db/schema.sql` |
| Seeders | `src/main/resources/db/seed.sql` (categories only) |
| `php artisan migrate` / SQLite data | `target/myitsbroadcast-importer.jar` (one-way, read-only source) |

The request path is:

```text
Browser → EncodingFilter → CsrfFilter → AuthFilter → Servlet → Service → DAO
        → HikariCP → PostgreSQL
        ← JSP/JSTL view (WEB-INF/views) ← request attributes
```

JSP files are views only. They contain no scriptlets and no SQL.

---

## 2. Route map

Laravel routes come from `routes/web.php`, `routes/settings.php`, and Fortify's
auto-registered auth routes.

| Laravel route | Method | JSP route | Servlet | Notes |
|---|---|---|---|---|
| `/` (`home`) | GET | `/`, `/feed` | `FeedServlet` | Published feed, search, filters, pagination |
| `/posts/{post}` | GET | `/posts/{slug}` | `PostServlet` | Published post or author-owned draft |
| `/posts/create` | GET | `/posts/new` | `PostServlet` | Create form |
| `/posts` | POST | `/posts` | `PostServlet` | Create; draft or published |
| `/posts/{post}/edit` | GET | `/posts/{slug}/edit` | `PostServlet` | Owner-only edit form |
| `/posts/{post}` | PUT | `/posts/{slug}` (`_method=PUT`) | `PostServlet` | Owner-only update |
| `/posts/{post}` | DELETE | `/posts/{slug}` (`_method=DELETE`) | `PostServlet` | Owner-only delete |
| `/posts/{post}/comments` | POST | `/comments` | `CommentServlet` | Comment or one-level reply |
| `/comments/{comment}` | DELETE | `/comments/{id}` (`_method=DELETE`) | `CommentServlet` | Author-only delete |
| — (new) | PUT | `/comments/{id}` (`_method=PUT`) | `CommentServlet` | Author-only edit (the one approved addition) |
| `/posts/{post}/bookmarks` | POST | `/bookmarks` | `BookmarkServlet` | Toggle; JSON for fetch, redirect otherwise |
| `/posts/{post}/bookmarks` | DELETE | `/bookmarks` (`_method=DELETE`) | `BookmarkServlet` | Toggle remove |
| `/dashboard` | GET | `/dashboard` | `DashboardServlet` | Own posts, counts, bookmarks |
| `/login` | GET/POST | `/login` | `LoginServlet` | Throttled, session fixation protection, remember-me |
| `/register` | GET/POST | `/register` | `RegisterServlet` | Immediate access; no verification |
| `/logout` | POST | `/logout` | `LogoutServlet` | Kept outside the shared navbar |
| `/forgot-password` | — | — | — | Removed: no reset flow in the JSP application |
| `/reset-password/{token}` | — | — | — | Removed: no reset flow in the JSP application |
| `/user/confirm-password` | GET/POST | `/confirm-password` | `ConfirmPasswordServlet` | Recent-password gate for security settings |
| `/settings` | GET | `/settings` | `SettingsServlet` | Redirect to profile |
| `/settings/profile` | GET/POST | `/settings/profile` | `ProfileServlet` | Name, email, avatar |
| (avatar upload) | POST | `/settings/profile/avatar` | `AvatarServlet` | Upload or remove |
| `/settings/security` | GET/POST | `/settings/security` | `SecurityServlet` | Password change |
| (account deletion) | POST | `/settings/account` | `AccountServlet` | Password confirmation required |
| — (static) | GET | `/uploads/*` | `UploadServlet` | Streams files from `APP_STORAGE_DIR` |
| — (ops) | GET | `/health` | `HealthServlet` | JSON health probe |

Laravel's `verified` middleware is deliberately **not** ported. The source already
comments it out; accounts work immediately after registration.

---

## 3. Entity and field map

### Users

| Laravel column | PostgreSQL column | Notes |
|---|---|---|
| `id` | `id BIGSERIAL` | IDs preserved by the importer |
| `name` | `name VARCHAR(255)` | Required, max 255 |
| `email` | `email VARCHAR(255)` | Unique, case-insensitive lookup |
| `email_verified_at` | — | Omitted; verification is excluded |
| `password` | `password VARCHAR(255)` | BCrypt; `$2y$` accepted on verify |
| `remember_token` | `remember_token VARCHAR(100)` | Persistent login validator hash |
| `is_moderator` | `is_moderator BOOLEAN` | Preserved; no new moderator UI |
| `avatar_path` | `avatar_path VARCHAR(255)` | Relative path under `APP_STORAGE_DIR` |
| `two_factor_*` | — | No working user-facing flow in source; not ported |
| `created_at`, `updated_at` | same | Naive `Asia/Jakarta` timestamps |

`passkeys` is not created: the source contains no working passkey UI.

### Categories

| Laravel column | PostgreSQL column | Notes |
|---|---|---|
| `id`, `name`, `slug`, `description` | same | Read-only in the JSP app |
| `created_at`, `updated_at` | same | Preserved |

### Posts

| Laravel column | PostgreSQL column | Notes |
|---|---|---|
| `id`, `user_id`, `category_id` | same | FKs; user delete cascades, category delete restricted |
| `type` | `type VARCHAR(20)` | `announcement` or `event` |
| `status` | `status VARCHAR(20)` | `draft`, `published`, `archived` |
| `title` | `title VARCHAR(180)` | Required, max 180 |
| `slug` | `slug VARCHAR(200)` | Unique; regenerated on title change |
| `body` | `body TEXT` | Required, max 20,000 |
| `cover_image_path` | `cover_image_path VARCHAR(255)` | Upload/remove |
| `is_pinned` | `is_pinned BOOLEAN` | Preserved data; no pinned UI |
| `published_at` | `published_at TIMESTAMP` | Visibility gate |
| `event_starts_at`, `event_ends_at`, `event_location` | same | Event posts only |
| `created_at`, `updated_at` | same | Preserved |

### Comments

| Laravel column | PostgreSQL column | Notes |
|---|---|---|
| `id`, `post_id`, `user_id` | same | FKs; cascade on post/user delete |
| `parent_id` | `parent_id BIGINT NULL` | Self-FK; one reply level |
| `body` | `body TEXT` | Required, max 2,000; non-blank check |
| `created_at`, `updated_at` | same | Edit updates only `body` and `updated_at` |

### Bookmarks

| Laravel column | PostgreSQL column | Notes |
|---|---|---|
| `id`, `user_id`, `post_id` | same | Unique `(user_id, post_id)` |
| `created_at`, `updated_at` | same | Preserved |

### Password reset tokens

Laravel ships `password_reset_tokens(email, token, created_at)`. The JSP application
does **not** create or use this table: password reset is removed, and password changes
happen in account settings with the current password.

### Deliberately excluded

- `event_registrations` — not created, not imported, no route/model/DAO/service/UI.
- `password_reset_tokens` — no reset flow, no token table, no mail delivery.
- `sessions`, `cache`, `jobs`, `failed_jobs` — Laravel infrastructure, not product data.
- `passkeys` and `two_factor_*` — no working user-facing flow in the source snapshot.
- `email_verified_at` — verification is excluded.

---

## 4. Validation and authorization parity

| Rule | Laravel | JSP |
|---|---|---|
| Registration name | required, max 255 | `AuthService` |
| Registration email | required, email, max 255, unique | `AuthService` + `UserDao.emailExists` |
| Registration password | confirmed, min 8 (dev policy) | `AuthService` + `Passwords` |
| Login throttle | 5 attempts / 60 s | `Throttle` |
| Post title | required, max 180 | `PostService` |
| Post body | required, max 20,000 | `PostService` |
| Post category | must exist | `CategoryDao.exists` |
| Post type | `announcement`/`event` | `PostType` |
| Event fields | required for event, end ≥ start, location max 255 | `PostService` |
| Comment body | required, max 2,000 | `CommentService` |
| Reply parent | must belong to the same post | `CommentService.create` |
| Post edit/delete | author only | `PostService.canEdit` + servlet guard |
| Comment edit/delete | author only | `CommentService.canEdit` |
| Draft visibility | author only | `PostService.findVisible` |
| Feed visibility | published + `published_at <= now` | `PostDao.publishedPage` |
| Profile email uniqueness | unique excluding self | `ProfileService` |
| Account deletion | current password | `ProfileService.deleteAccount` |
| Security settings | recent password confirmation | `ConfirmPasswordServlet.recentlyConfirmed` |
| Uploads | JPEG/PNG/WebP, 2 MB, magic bytes | `Uploads` |

---

## 5. Data import

The importer is a separate runnable jar:

```bash
mvn -q -DskipTests package
java -jar target/myitsbroadcast-importer.jar --dry-run
java -jar target/myitsbroadcast-importer.jar
```

Properties:

- Opens `database/database.sqlite` **read-only**.
- Never writes to the SQLite file.
- Reads `DB_URL`, `DB_USER`, `DB_PASSWORD` from the environment for the destination.
- Imports users, categories, posts, comments, bookmarks in one transaction.
- Preserves primary keys, ownership, reply parentage, timestamps, and image paths.
- Copies legacy uploaded images into `APP_STORAGE_DIR` when present.
- Resets PostgreSQL sequences after the import.
- Verifies row counts before commit.
- **Skips `event_registrations` entirely.**
- `--replace` clears only the destination tables first; it is opt-in and must never be
  used against a database with data you need. The default is an id-preserving upsert.

Dry-run evidence (2026-11-25):

```text
users              14
categories          6
posts              15
comments            9
bookmarks           5
event registrations 5 (skipped by design)
Dry run complete. No PostgreSQL connection was opened and nothing was written.
```

The import itself has **not** been executed because PostgreSQL credentials and approval
were not supplied.

---

## 6. Intentional differences

1. **No email verification.** No page, prompt, gate, or `email_verified_at` column.
2. **No navbar logout.** Logout remains at `/logout` and is linked only from
   `settings/profile.jsp` and `settings/security.jsp`.
3. **Footer wordmark only.** No description, links, copyright, or social icons.
4. **Comment editing added.** Author-only inline edit; the only approved product addition.
5. **Event registration removed.** The legacy model/migration/factory remain in the
   Laravel archive but are absent from the JSP application and schema.
6. **No 2FA/passkey UI.** The source snapshot contains no working user-facing flow.
7. **Password reset removed.** No forgot/reset pages, servlets, token table, or mail
   delivery. Password changes remain available in account settings.
8. **Uploads live outside the WAR.** `APP_STORAGE_DIR` is persistent and served through
   `/uploads/*`, so redeploying the WAR does not delete user images.
9. **Timestamps are naive `Asia/Jakarta`.** This matches Laravel's configured timezone.
