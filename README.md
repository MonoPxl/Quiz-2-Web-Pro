# myITS Broadcast

myITS Broadcast is a campus announcement and event broadcaster for the Institut Teknologi
Sepuluh Nopember (ITS) community. It publishes announcements and event information in a
public, newest-first feed, supports keyword/category/type filtering and pagination, and
lets signed-in users publish posts, save drafts, comment and reply, bookmark posts, and
manage their own content from a personal dashboard.

**The final application is a Java 17 / Jakarta EE 10 JSP web application.** It uses
Servlet 6.0, JSP 3.1, JSTL 3.0, Tomcat 10.1, Maven WAR packaging, and PostgreSQL through
JDBC/HikariCP. The original Laravel implementation remains in this repository as a
behavioural and data reference; it is not the runtime application.

> **Status:** the WAR builds, passes 49 automated tests, and has been started on Tomcat
> 10.1.34. The PostgreSQL schema and importer are ready, but **no database connection or
> import has been executed** because credentials and approval were not supplied. See
> [`docs/AUDIT.md`](docs/AUDIT.md) for the honest requirement-by-requirement status.

---

## Technology

| Layer | Choice |
|---|---|
| Language | Java 17 |
| Web APIs | Jakarta Servlet 6.0, JSP 3.1, JSTL 3.0 |
| Container | Apache Tomcat 10.1 (Jakarta namespace) |
| Build | Maven, `war` packaging |
| Database | PostgreSQL via JDBC 42.7.4 + HikariCP 5.1.0 |
| Passwords | jBCrypt (BCrypt cost 12) |
| Views | JSP + JSTL + EL, no scriptlets, no SQL |
| Assets | Hand-written CSS + vanilla JS; Instrument Sans bundled locally |
| Tests | JUnit 5, embedded Tomcat, Jasper JSP precompilation |

No Tailwind, Bootstrap, React, Vue, Spring Boot, PHP, or Laravel runtime is used by the
JSP application.

---

## Features

- Public feed of published announcements/events, newest first, 9 per page
- Keyword search plus category and type filters, with query-preserving pagination
- Post detail with event date/time/location metadata, cover image, comments, bookmarks
- Create/edit/delete own posts, including drafts, publish transitions, and cover images
- One-level comments and replies, with author-only edit and delete
- Bookmark toggle, duplicate prevention, and a saved-post list on the dashboard
- Personal dashboard with real post/published/draft/bookmark counts
- Registration, login, logout, profile/avatar, password change, account deletion
- No email verification; accounts work immediately after registration
- No event registration/RSVP anywhere; event posts are broadcast information only

---

## Quick start

### Prerequisites

- JDK 17
- Maven 3.9+
- PostgreSQL 14+ (only when you are ready to initialize a database)
- Apache Tomcat 10.1 (the helper script can download it)

On Windows, the verified toolchain used for this project lives under
`%USERPROFILE%\dev` (Temurin 17 and Maven 3.9.9). Maven is not required on `PATH` if you
set `JAVA_HOME` and `PATH` for the session.

### 1. Configure environment

Copy the JSP template and fill in real values. `.env.jsp` is Git-ignored. The legacy
Laravel app keeps its own `.env`, so the two runtimes cannot overwrite each other's
settings.

```powershell
Copy-Item .env.jsp.example .env.jsp
```

```bash
cp .env.jsp.example .env.jsp
```

Required keys:

| Variable | Purpose |
|---|---|
| `APP_ENV` | `development` or `production` |
| `APP_BASE_URL` | Public base URL used in absolute links and redirects |
| `APP_STORAGE_DIR` | Persistent directory for avatars and covers |
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/myitsbroadcast` |
| `DB_USER` | PostgreSQL user |
| `DB_PASSWORD` | PostgreSQL password |
| `DB_POOL_SIZE` | HikariCP pool size (optional) |
| `LOGIN_MAX_ATTEMPTS`, `LOGIN_WINDOW_SECONDS` | Login throttling |

Never commit `.env.jsp` or paste real credentials into documentation.

### 2. Initialize PostgreSQL (when approved)

The schema is idempotent and never drops data. Apply it once:

```bash
# Replace the user/database names with the values inside your DB_URL.
psql -h localhost -U myitsbroadcast_app -d myitsbroadcast \
  -f src/main/resources/db/schema.sql
psql -h localhost -U myitsbroadcast_app -d myitsbroadcast \
  -f src/main/resources/db/seed.sql
```

`seed.sql` inserts the six campus categories. Users, posts, comments, and bookmarks come
from the legacy SQLite database through the importer (below), or from normal use.

### 3. Build and test

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\dev\jdk-17.0.21.1+2"
$env:PATH = "$env:JAVA_HOME\bin;$env:USERPROFILE\dev\apache-maven-3.9.9\bin;$env:PATH"
mvn test
mvn package
```

```bash
mvn test
mvn package
```

Outputs:

- `target/myitsbroadcast.war` — deployable web application
- `target/myitsbroadcast-importer.jar` — runnable SQLite-to-PostgreSQL importer

### 4. Run locally on Tomcat

**Linux/macOS/Git Bash:**

```bash
./scripts/run-local.sh
```

**Windows PowerShell:**

```powershell
$tc = "$env:USERPROFILE\dev\apache-tomcat-10.1.34"
Copy-Item target\myitsbroadcast.war "$tc\webapps\ROOT.war" -Force
$env:CATALINA_HOME = $tc
$env:JAVA_HOME = "$env:USERPROFILE\dev\jdk-17.0.21.1+2"
& "$tc\bin\catalina.bat" run
```

Open <http://localhost:8080/>. The health probe is <http://localhost:8080/health>.

The helper script downloads Tomcat 10.1.34 into `~/.local` if it is missing, binds the
container to `$PORT` (default 8080), deploys the WAR as `ROOT`, and stores Tomcat runtime
files under `storage-jsp/tomcat` (Git-ignored).

---

## Legacy data import

The importer reads `database/database.sqlite` **read-only** and writes to PostgreSQL in a
single transaction. It preserves ids, ownership, reply parentage, timestamps, and image
paths, resets sequences, and verifies counts before commit. It deliberately skips
`event_registrations`.

```bash
# Safe preview: opens no PostgreSQL connection and writes nothing
java -jar target/myitsbroadcast-importer.jar --dry-run

# Real import (requires DB_URL, DB_USER, DB_PASSWORD)
java -jar target/myitsbroadcast-importer.jar

# Opt-in destination clear, then import; use only on a disposable database
java -jar target/myitsbroadcast-importer.jar --replace
```

Current dry-run counts: 14 users, 6 categories, 15 posts, 9 comments, 5 bookmarks, and
5 event registrations skipped by design.

The legacy Laravel source and SQLite file are preserved. Do not delete them until the JSP
application has been verified against a real database.

---

## Project layout

```text
.
├── pom.xml                     # Maven WAR + importer assembly
├── .env.jsp.example            # JSP configuration template (Laravel keeps .env)
├── domcloud.yml                # Sample DOMCloud deployment script
├── scripts/
│   ├── run-local.sh            # Build + run Tomcat locally
│   └── domcloud-start.sh       # Passenger start script ($PORT)
├── src/main/java/id/myitsbroadcast/
│   ├── config/                 # AppConfig, HikariCP
│   ├── model/                  # User, Category, Post, Comment, Bookmark, Page
│   ├── dao/                    # JDBC DAOs, PreparedStatement only
│   ├── service/                # Business rules and authorization
│   ├── web/                    # Servlets/controllers
│   ├── filter/                 # UTF-8, CSRF, authentication
│   ├── util/                   # Auth, Passwords, Uploads, Flash, Csrf, etc.
│   └── migration/              # Read-only SQLite importer
├── src/main/resources/db/
│   ├── schema.sql              # Idempotent PostgreSQL schema
│   └── seed.sql                # Six campus categories
├── src/main/webapp/
│   ├── WEB-INF/web.xml
│   ├── WEB-INF/views/          # JSP views (not directly accessible)
│   ├── WEB-INF/tags/           # avatar, postCard, pagination
│   └── assets/                 # CSS, JS, SVG, local fonts
├── src/test/java/              # Unit, schema, JSP audit, embedded Tomcat smoke tests
├── docs/                       # Audit, report, ERDs, migration map, screenshots
├── app/ resources/ routes/ ... # Legacy Laravel reference (not the runtime)
└── database/database.sqlite    # Legacy data source (read-only for the importer)
```

---

## Documentation

| Document | Contents |
|---|---|
| [`docs/AUDIT.md`](docs/AUDIT.md) | Requirement-by-requirement IMPLEMENTED / PARTIAL / NOT IMPLEMENTED audit |
| [`docs/MIGRATION_MAP.md`](docs/MIGRATION_MAP.md) | Laravel route/field/rule mapping and import notes |
| [`docs/erd-conceptual.md`](docs/erd-conceptual.md) | Conceptual ERD and relationships |
| [`docs/erd-physical.md`](docs/erd-physical.md) | PostgreSQL tables, columns, constraints, indexes, delete rules |
| [`docs/screenshots/`](docs/screenshots/) | Real Tomcat screenshots of auth and error pages |

---

## Deployment (DOMCloud)

The application is **not deployed**. `domcloud.yml` and `scripts/domcloud-start.sh` are a
documented, reviewable path:

1. Push this repository to GitHub.
2. Replace the `source:` placeholder in `domcloud.yml`.
3. Apply the script in DOMCloud's Setup → Deploy tab (or configure the same values).
4. Run `schema.sql` once over SSH after the first deploy.
5. Import legacy data only after taking a backup.
6. Smoke test `/health`, `/login`, `/register`, assets, and an unknown URL.

DOMCloud does not preinstall Tomcat just because Java is enabled; the start script
downloads Tomcat 10.1.34 into `~/.local`. Plan eligibility for the free tier and the
`java`/`postgresql` features was **not** verified against the user's account.

---

## Security notes

- All SQL values are bound with `PreparedStatement`; no user input is concatenated.
- Passwords use BCrypt cost 12. Legacy Laravel `$2y$` hashes are accepted on verify.
- Login rotates the session id and is throttled at 5 attempts / 60 s per email+IP.
- CSRF tokens protect every state-changing request, including multipart forms.
- JSP output uses `<c:out>`; views contain no scriptlets or SQL.
- Uploads are validated by magic bytes, limited to 2 MB, renamed with UUIDs, and confined
  to `APP_STORAGE_DIR`.
- Drafts are author-only; post and comment mutations are author-only.
- Password changes require the current password and are available only from account
  settings; there is no self-service reset flow.

---

## Group

The original project README listed the following members. Percentages and JSP-specific
contributions must be confirmed by the group; they are not invented here.

| Name | NRP | Original role |
|---|---|---|
| Marvel Timothy N. | 5025251209 | Backend, database design, authentication |
| Joel Angga Fransmartua Manalu | 5025251212 | Features, testing, final report |
| Syabil Zihni | 5025251217 | Frontend, UI/UX, project documentation |
---

## Legacy Laravel application

The original Laravel 13 / Livewire / Blade / Tailwind implementation is retained for
reference and data recovery. Its routes, controllers, policies, migrations, views, and
`database/database.sqlite` are unchanged. It is **not** the application to run for Quiz 2,
and it must not be treated as a second active deployment.

The JSP migration intentionally:

- removes email verification;
- removes password reset (no forgot/reset pages, tokens, or mail delivery);
- removes event registration/RSVP;
- keeps logout out of the shared navbar but preserves `/logout`;
- reduces the footer to the `myITS Broadcast` wordmark;
- adds author-only comment editing (the only approved product addition);
- keeps the blue-first design system from the concept board.
