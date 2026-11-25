-- ===========================================================================
-- myITS Broadcast — PostgreSQL schema
-- ---------------------------------------------------------------------------
-- Run against an empty application database, for example:
--   psql -h localhost -p 5432 -U myitsbroadcast_app -d myitsbroadcast \
--        -f src/main/resources/db/schema.sql
--
-- The script is idempotent: it creates objects only when they do not exist.
-- It never drops data. Email verification is intentionally absent from this
-- schema; accounts are usable immediately after registration.
-- Password reset is intentionally absent: accounts are created and maintained
-- inside the application, and no reset-token table is provisioned.
-- Event registration is intentionally absent: myITS Broadcast only publishes
-- event information, it does not register attendees.
-- ===========================================================================

BEGIN;

CREATE TABLE IF NOT EXISTS users (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    password       VARCHAR(255) NOT NULL,
    remember_token VARCHAR(100),
    is_moderator   BOOLEAN NOT NULL DEFAULT FALSE,
    avatar_path    VARCHAR(255),
    created_at     TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Jakarta'),
    updated_at     TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Jakarta')
);

CREATE UNIQUE INDEX IF NOT EXISTS users_email_unique ON users (email);

CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Jakarta'),
    updated_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Jakarta')
);

CREATE UNIQUE INDEX IF NOT EXISTS categories_name_unique ON categories (name);
CREATE UNIQUE INDEX IF NOT EXISTS categories_slug_unique ON categories (slug);

CREATE TABLE IF NOT EXISTS posts (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id      BIGINT NOT NULL REFERENCES categories (id) ON DELETE RESTRICT,
    type             VARCHAR(20) NOT NULL DEFAULT 'announcement',
    status           VARCHAR(20) NOT NULL DEFAULT 'draft',
    title            VARCHAR(180) NOT NULL,
    slug             VARCHAR(200) NOT NULL,
    body             TEXT NOT NULL,
    cover_image_path VARCHAR(255),
    is_pinned        BOOLEAN NOT NULL DEFAULT FALSE,
    published_at     TIMESTAMP,
    event_starts_at  TIMESTAMP,
    event_ends_at    TIMESTAMP,
    event_location   VARCHAR(255),
    created_at       TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Jakarta'),
    updated_at       TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Jakarta'),
    CONSTRAINT posts_type_check CHECK (type IN ('announcement', 'event')),
    CONSTRAINT posts_status_check CHECK (status IN ('draft', 'published', 'archived'))
);

CREATE UNIQUE INDEX IF NOT EXISTS posts_slug_unique ON posts (slug);
CREATE INDEX IF NOT EXISTS posts_user_id_index ON posts (user_id);
CREATE INDEX IF NOT EXISTS posts_category_id_index ON posts (category_id);
CREATE INDEX IF NOT EXISTS posts_status_published_at_index ON posts (status, published_at);
CREATE INDEX IF NOT EXISTS posts_category_id_published_at_index ON posts (category_id, published_at);
CREATE INDEX IF NOT EXISTS posts_type_index ON posts (type);

CREATE TABLE IF NOT EXISTS comments (
    id         BIGSERIAL PRIMARY KEY,
    post_id    BIGINT NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    user_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    parent_id  BIGINT REFERENCES comments (id) ON DELETE CASCADE,
    body       TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Jakarta'),
    updated_at TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Jakarta'),
    CONSTRAINT comments_body_check CHECK (length(btrim(body)) > 0)
);

CREATE INDEX IF NOT EXISTS comments_post_id_index ON comments (post_id);
CREATE INDEX IF NOT EXISTS comments_user_id_index ON comments (user_id);
CREATE INDEX IF NOT EXISTS comments_parent_id_index ON comments (parent_id);
CREATE INDEX IF NOT EXISTS comments_post_id_created_at_index ON comments (post_id, created_at);

CREATE TABLE IF NOT EXISTS bookmarks (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    post_id    BIGINT NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Jakarta'),
    updated_at TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Jakarta')
);

CREATE UNIQUE INDEX IF NOT EXISTS bookmarks_user_id_post_id_unique ON bookmarks (user_id, post_id);
CREATE INDEX IF NOT EXISTS bookmarks_user_id_index ON bookmarks (user_id);
CREATE INDEX IF NOT EXISTS bookmarks_post_id_index ON bookmarks (post_id);

COMMIT;
