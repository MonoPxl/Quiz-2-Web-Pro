-- ===========================================================================
-- myITS Broadcast — reference seed data (categories only)
-- ---------------------------------------------------------------------------
-- Categories are the only rows the original application seeds directly.
-- Users, posts, comments and bookmarks are migrated from the legacy SQLite
-- database with the importer (see docs/MIGRATION_MAP.md).
--
-- Idempotent: re-running updates the existing category rows, adds nothing else.
-- ===========================================================================

BEGIN;

INSERT INTO categories (name, slug, description) VALUES
    ('Akademik', 'akademik', 'Academic announcements, schedules, and campus policy updates.'),
    ('Kegiatan Kampus', 'kegiatan-kampus', 'Events, seminars, and activities held around ITS.'),
    ('Kompetisi', 'kompetisi', 'Competitions and challenges for ITS students.'),
    ('Organisasi', 'organisasi', 'Student organizations, UKM, and recruitment information.'),
    ('Beasiswa', 'beasiswa', 'Scholarship opportunities and financial aid updates.'),
    ('Karier', 'karier', 'Internships, career fairs, and job opportunities.')
ON CONFLICT (slug) DO UPDATE
    SET name = EXCLUDED.name,
        description = EXCLUDED.description,
        updated_at = (now() AT TIME ZONE 'Asia/Jakarta');

COMMIT;
