# Implementation audit — myITS Broadcast JSP migration

Audit date: 2026-11-25
Source of truth: `MYITSBROADCAST_BUILD_PROMPT.md`
Method: code inspection, Maven build/tests, Tomcat 10.1.34 deployment, HTTP smoke tests,
JSP precompilation, and screenshots of the pages that do not require a database.

Legend:

- **IMPLEMENTED** — present in the JSP application and supported by code/tests/build evidence.
- **PARTIALLY IMPLEMENTED** — present, but a specific sub-item is missing or cannot be verified.
- **NOT IMPLEMENTED** — absent by design or a real gap.

> Runtime note: the user has not approved a PostgreSQL connection or data import. Every
> database-backed flow is therefore verified by code review, schema checks, DAO/service
> unit tests, and JSP compilation — not by a live end-to-end request. This is called out
> explicitly wherever it applies. Static pages, error pages, assets, JSP compilation,
> and Tomcat startup are verified against a real Tomcat instance.

---

## 0. Non-negotiable decisions

| Requirement | Status | Evidence |
|---|---|---|
| Final runtime is JSP, not Laravel | IMPLEMENTED | `pom.xml` WAR, `src/main/webapp/WEB-INF/views/*.jsp`, Jakarta servlets; Tomcat 10.1.34 started the WAR and served JSPs |
| Product stays a broadcaster | IMPLEMENTED | No registration/RSVP route, servlet, DAO, service, table, or UI |
| Preserve existing features; only add own-comment edit | IMPLEMENTED | Route/feature inventory in `docs/MIGRATION_MAP.md`; comment edit is the only added capability |
| Blue-first direction and three UI requests | IMPLEMENTED | Token palette in `assets/css/app.css`; no verification UI; no navbar logout; footer wordmark only; screenshots in `docs/screenshots/` |

## 1. Target stack

| Requirement | Status | Evidence |
|---|---|---|
| Java 17 | IMPLEMENTED | `<maven.compiler.release>17</maven.compiler.release>`; `java -version` 17.0.21.1 |
| Jakarta Servlet 6.0 | IMPLEMENTED | `jakarta.servlet-api:6.0.0` (`provided`) |
| JSP 3.1 | IMPLEMENTED | `jakarta.servlet.jsp-api:3.1.0` (`provided`); JSPs compiled by Jasper |
| JSTL 3.0 | IMPLEMENTED | `jakarta.servlet.jsp.jstl-api:3.0.0` + Glassfish impl 3.0.1 |
| Tomcat 10.1 | IMPLEMENTED | Tomcat 10.1.34 downloaded and used for the smoke run |
| Maven WAR packaging | IMPLEMENTED | `mvn package` produced `target/myitsbroadcast.war` |
| PostgreSQL JDBC 42.7.x | IMPLEMENTED | `org.postgresql:postgresql:42.7.4` |
| Small connection pool | IMPLEMENTED | HikariCP 5.1.0 in `config/Db.java` |
| Credentials only from environment | IMPLEMENTED | `config/AppConfig.java` reads `DB_URL`, `DB_USER`, `DB_PASSWORD`; no credentials in source |
| jBCrypt password hashing | IMPLEMENTED | `util/Passwords.java`, BCrypt cost 12; Laravel `$2y$` normalized to `$2a$` on verify |
| Plain CSS + vanilla JS only | IMPLEMENTED | `assets/css/app.css`, `assets/js/app.js`; no Tailwind/Bootstrap/React/Vue/Spring in the JSP runtime |
| No scriptlets or SQL in JSP | IMPLEMENTED | `JspAuditTest`; repository grep finds no `<% ... %>` scriptlets and no SQL statements in JSP/tag files |
| `jakarta.*` imports | IMPLEMENTED | All servlet and filter imports use `jakarta.*` |

## 2. Safe migration

| Requirement | Status | Evidence |
|---|---|---|
| Inspect Git state before editing | IMPLEMENTED | `git status --short`, branch and recent commits inspected before work |
| Inspect real Laravel routes/controllers/models | IMPLEMENTED | `routes/web.php`, `routes/settings.php`, Fortify routes, controllers, policies, models inspected |
| Migration inventory | IMPLEMENTED | `docs/MIGRATION_MAP.md` |
| Preserve Laravel source and SQLite | IMPLEMENTED | Laravel tree untouched; `database/database.sqlite` untouched; all work is additive/uncommitted |
| Build/verify before removing Laravel | IMPLEMENTED | Nothing was deleted; the JSP app builds and runs alongside the legacy tree |
| JSP is the clearly identified final app | IMPLEMENTED | `README.md` presents the JSP/Maven application as the final deliverable; the Laravel tree is explicitly labelled a legacy/reference archive, not a second active runtime |
| No history/assets deleted | IMPLEMENTED | No `git rm`, no history rewrite |

## 3. Broadcaster behavior

| Requirement | Status | Evidence |
|---|---|---|
| Public feed: published only, newest first, paginated | IMPLEMENTED (code) | `PostDao.publishedPage`: `status = 'published' AND published_at IS NOT NULL AND published_at <= ?`, `ORDER BY published_at DESC, id DESC`, `LIMIT/OFFSET`; `PostService.PER_PAGE = 9` |
| Search, category/type filters, combined filters | IMPLEMENTED (code) | `PostDao` builds `title ILIKE`/`body ILIKE`, category slug, type; `FeedServlet` forwards `q`, `category`, `type` |
| Clear filters | IMPLEMENTED (code) | `feed.jsp` clear link resets the query string |
| Pagination preserves query parameters | IMPLEMENTED (code) | `pagination.tag` rebuilds links with current filters |
| Post detail; drafts author-only | IMPLEMENTED (code) | `PostService.findVisible`; `PostPolicy` parity |
| Event posts show existing date/time/location | IMPLEMENTED (code) | `posts/show.jsp` conditional event block; `PostService` event validation |
| Create/edit/delete own posts | IMPLEMENTED (code) | `PostServlet`, `PostService`; owner checks in service/servlet |
| Draft/published status | IMPLEMENTED (code) | `PostStatus`; publish sets `published_at`, draft clears it |
| Cover upload/removal | IMPLEMENTED (code) | `Uploads`, `PostService.update`, `PostServlet` |
| Comments/replies, one level | IMPLEMENTED (code) | `CommentService.create` flattens replies to the top-level parent |
| Delete own comment | IMPLEMENTED (code) | `CommentService.canEdit` → `comments.delete`; author-only, matching `CommentPolicy` |
| Edit own comment/reply | IMPLEMENTED (code) | `CommentService.update`; `CommentServlet` PUT; inline editor in `posts/show.jsp`; tests in `CommentServiceTest` |
| Another user cannot edit | IMPLEMENTED (code + tests) | `CommentServiceTest` covers other-user and unauthenticated attempts |
| Moderators gain no new edit rights | IMPLEMENTED (code) | Laravel `CommentPolicy` is author-only; JSP mirrors it |
| Bookmarks add/remove, list, uniqueness | IMPLEMENTED (code) | `BookmarkService.toggle`, `BookmarkDao`, unique index `(user_id, post_id)` |
| Dashboard counts match real rows | IMPLEMENTED (code) | `DashboardService` computes from `latestForUser` and `listForUser`; no fake metrics |
| Registration/login/logout | IMPLEMENTED (code) | `RegisterServlet`, `LoginServlet`, `LogoutServlet`; logout outside navbar |
| Password change | IMPLEMENTED (code) | `SecurityServlet` + `SecurityService`; current password required, BCrypt re-hash; available only from account settings |
| Password reset | ABSENT BY DESIGN | No forgot/reset servlet, view, token table, or mail delivery; login page has no reset link |
| Profile edit, avatar upload/remove | IMPLEMENTED (code) | `ProfileServlet`, `AvatarServlet`, `ProfileService`, `Uploads` |
| Account deletion with password | IMPLEMENTED (code) | `AccountServlet`, `ProfileService.deleteAccount` |
| Existing security settings | IMPLEMENTED (code) | `SecurityServlet` + recent-password confirmation; no 2FA/passkey UI exists in source, so none was invented |
| No category administration | IMPLEMENTED (by omission) | Categories are read-only in `CategoryDao`; no CRUD UI |
| `is_pinned` preserved but not surfaced | IMPLEMENTED | Column retained; no pinned UI or ordering |
| `is_moderator` preserved, no moderator module | IMPLEMENTED | Column retained; no moderator dashboard |

## 4. Explicit exclusions and UI requests

| Requirement | Status | Evidence |
|---|---|---|
| No event registration/RSVP table | IMPLEMENTED | `schema.sql` creates only users, categories, posts, comments, and bookmarks |
| No event registration route/servlet/DAO/service/form/button | IMPLEMENTED | No `EventRegistration` classes or routes in `src/`; grep only finds importer comments/skip logic and negative tests |
| Importer skips event registrations | IMPLEMENTED | `Importer` reads counts but never copies `event_registrations`; dry run reports `5 (skipped by design)` |
| Event posts remain broadcast-only | IMPLEMENTED | Event fields are read-only metadata; no RSVP control |
| No email-verification page/prompt/gate | IMPLEMENTED | No verification servlet/view; `email_verified_at` absent from schema; registration logs the user in immediately |
| No Logout in shared navbar | IMPLEMENTED | `partials/head.jsp`, `partials/nav-links.jsp`, `partials/account-controls.jsp` contain no logout; only `settings/profile.jsp` and `settings/security.jsp` do |
| Logout endpoint preserved | IMPLEMENTED | `LogoutServlet` at `/logout` |
| Footer only “myITS Broadcast” | IMPLEMENTED | `partials/foot.jsp` renders one wordmark paragraph; verified in Tomcat HTML and screenshots |
| Blue-first palette follows concept board | IMPLEMENTED | `:root` defines `--blue-950/900/700/500/100`, `--cyan-500`, `--gold-400`, `--canvas`, `--surface`, `--text`, `--muted`, `--line`; Instrument Sans bundled locally |
| No unrelated new product feature | IMPLEMENTED | Only comment editing added; no likes, follows, notifications, admin, etc. |

## 5. Security

| Requirement | Status | Evidence |
|---|---|---|
| PreparedStatement for all values | IMPLEMENTED | `Jdbc` binds parameters; DAOs use placeholders |
| No user input concatenated into SQL | IMPLEMENTED | Dynamic filters append fixed SQL fragments and bind values |
| BCrypt hashing | IMPLEMENTED | `Passwords`; cost 12 |
| Session fixation protection | IMPLEMENTED | `Auth.login` invalidates and creates a new session |
| Secure session invalidation on logout | IMPLEMENTED | `LogoutServlet` + `Auth.logout` |
| CSRF on every state-changing request | IMPLEMENTED | `CsrfFilter` + `Csrf`; multipart action query token; tests in `CsrfTest` |
| Server-side authorization | IMPLEMENTED | Owner checks for posts/comments; draft visibility; bookmarks require login |
| Output escaping | IMPLEMENTED | `<c:out>` in views; `JspAuditTest` checks no raw `${post.body}`/comment output |
| Server-side validation | IMPLEMENTED | `PostService.validate`, `CommentService.validate`, `AuthService.validateRegistration`, `ProfileService` |
| Post/Redirect/Get + flash | IMPLEMENTED | Servlets redirect after POST; `Flash` |
| Safe uploads | IMPLEMENTED | Magic-byte validation, 2 MB limit, UUID filenames, storage-dir containment |
| UTF-8 everywhere | IMPLEMENTED | `web.xml` request/response encoding + JSP page encoding |
| Login throttling | IMPLEMENTED | `Throttle`, 5 attempts / 60 s per email+IP |

## 6. Design, responsive, accessibility

| Requirement | Status | Evidence |
|---|---|---|
| Desktop 1440 px verified | IMPLEMENTED (static pages) | `docs/screenshots/*-desktop.png` captured at 1440 px |
| Mobile 390 px verified | IMPLEMENTED (static pages) | `docs/screenshots/*-mobile.png` captured at 390 px via CDP device emulation |
| 360 px / tablet sanity check | IMPLEMENTED (static pages) | `not-found-404-360.png` at 360 px and `login-tablet.png` at 768 px; CSS breakpoints at 1040/860/640 px |
| No horizontal overflow | IMPLEMENTED (static pages) | Devtools measurement: `scrollWidth == viewport` at 390 px and 360 px; guest header and error card fit; auth layout collapses to one column |
| Semantic landmarks/labels | IMPLEMENTED | `<header>`, `<nav>`, `<main>`, `<footer>`, visible `<label>` elements, `aria-current` |
| Keyboard focus visible | IMPLEMENTED | `:focus-visible` ring in `app.css` |
| WCAG AA contrast | PARTIALLY IMPLEMENTED | Palette and text colors chosen for contrast; no automated axe/Lighthouse audit was run |
| `prefers-reduced-motion` | IMPLEMENTED | Reduced-motion media query in `app.css` |
| Light mode only | IMPLEMENTED | `color-scheme: light`; no theme toggle |
| Inline validation and empty states | IMPLEMENTED | Error blocks in views; empty states in feed/dashboard/comments |

## 7. Report and handoff

| Requirement | Status | Evidence |
|---|---|---|
| `docs/REPORT.md` | IMPLEMENTED | This repository |
| `docs/erd-conceptual.md` | IMPLEMENTED | This repository |
| `docs/erd-physical.md` | IMPLEMENTED | This repository |
| `docs/MIGRATION_MAP.md` | IMPLEMENTED | This repository |
| Screenshots of running JSP pages | PARTIALLY IMPLEMENTED | Login/register/404/500 captured from Tomcat; feed/detail/dashboard require PostgreSQL and are disclosed as pending |
| Feature list + explanations | IMPLEMENTED | `docs/REPORT.md` |
| Honest C/R/U/D matrix | IMPLEMENTED | `docs/REPORT.md`; ambiguity flagged |
| Architecture explanation | IMPLEMENTED | `docs/REPORT.md` |
| DOMCloud deployment explanation | IMPLEMENTED | `docs/REPORT.md`, `domcloud.yml`, `scripts/domcloud-start.sh` |
| Group contribution placeholders | IMPLEMENTED | `docs/REPORT.md`; names/NRPs carried from the repository README, percentages left as placeholders |
| PDF export | NOT IMPLEMENTED | No reliable Markdown-to-PDF/Mermaid toolchain was available; `docs/REPORT.md` is the source of truth |
| Distinguish implemented vs unverified | IMPLEMENTED | This audit and the runtime note in `docs/REPORT.md` |

## 8. Build and runtime evidence

| Check | Result |
|---|---|
| `mvn -q test` | Passed: 50 tests, 0 failures, 0 errors |
| `mvn -q package` | Passed: `target/myitsbroadcast.war` (19,211,294 bytes), `target/myitsbroadcast-importer.jar` (19,921,564 bytes) |
| `PORT=18081 bash scripts/domcloud-start.sh` | Generated `127.0.0.1:18081`, deployed the WAR built at that time (19,993,492 bytes), `Server startup in [1170] milliseconds`; `/health` and `/login` returned 200 |
| Responsive measurement at 390 px and 360 px | `document.documentElement.scrollWidth == viewport`; no overflowing elements |
| Tomcat 10.1.34 startup | `Server startup in [1203] milliseconds` |
| `GET /health` | `200` → `{"status":"ok","application":"myitsbroadcast"}` |
| `GET /login` | `200 text/html` |
| `GET /register` | `200 text/html` |
| `GET /forgot-password` | `404` (feature removed) |
| `GET /assets/css/app.css` | `200 text/css` |
| `GET /assets/fonts/instrument-sans-400.woff2` | `200 font/woff2` |
| `GET /assets/img/signal.svg` | `200 image/svg+xml` |
| Unknown URL | `404` with the JSP error page |
| `GET /` without PostgreSQL | `500` with the JSP error page (expected: no DB credentials/approval) |
| JSP precompilation | `Generation completed with [0] errors` |
| Importer dry run | users 14, categories 6, posts 15, comments 9, bookmarks 5, event registrations 5 skipped; no PostgreSQL connection opened |

## 9. Real gaps and ambiguities

1. **No live PostgreSQL verification.** The user explicitly withheld database credentials and
   approval. Feed/detail/dashboard/post CRUD/comment/bookmark/profile runtime behavior is
   implemented and unit-tested where a seam exists, but not exercised end-to-end against
   PostgreSQL. The schema and importer are validated structurally; no import has been run.
2. **Screenshots of DB-backed pages are missing.** Only auth, error, and static pages could be
   captured. This is disclosed in the report rather than substituting concept-board images.
3. **“8 CRUD features” interpretation.** The app has full C/R/U/D for posts, comments, and
   users/profile; bookmarks have C/R/D; categories are read-only by design. Counting
   feature-level operations comfortably exceeds eight, but if the lecturer requires eight
   separate resources each with full CRUD, the existing product scope does not meet that
   interpretation. Adding resources would violate the no-new-features rule. The matrix in
   `docs/REPORT.md` states this plainly.
4. **PDF report not generated.** The Markdown report is complete; no reliable PDF/Mermaid
   export toolchain was available.
5. **Automated accessibility audit not run.** 1440, 768, 390, and 360 px were checked;
   contrast was designed but not machine-verified with axe/Lighthouse.
6. **2FA/passkeys.** The source snapshot contains no working user-facing 2FA/passkey flow,
   so none was ported. The legacy `passkeys` table is not created in the JSP schema.
