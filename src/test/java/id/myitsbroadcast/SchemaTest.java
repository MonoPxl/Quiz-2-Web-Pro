package id.myitsbroadcast;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the explicit exclusions that must not creep back into the schema. */
class SchemaTest {

    private static final Path SCHEMA = Path.of("src/main/resources/db/schema.sql");

    @Test
    void eventRegistrationIsAbsent() throws IOException {
        String schema = readWithoutComments();

        assertFalse(schema.toLowerCase().contains("event_registration"),
                "event registration must not exist in the JSP application");
        assertFalse(schema.toLowerCase().contains("rsvp"));
        assertFalse(schema.toLowerCase().contains("attendee"));
    }

    @Test
    void emailVerificationIsAbsent() throws IOException {
        assertFalse(read().toLowerCase().contains("email_verified_at"));
    }

    @Test
    void passwordResetTokensAreAbsent() throws IOException {
        assertFalse(read().toLowerCase().contains("password_reset"),
                "password reset is not part of the application");
    }

    @Test
    void productTablesAndRelationshipsExist() throws IOException {
        String schema = read();

        for (String table : new String[]{"users", "categories", "posts", "comments", "bookmarks"}) {
            assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS " + table),
                    "missing table " + table);
        }

        assertTrue(schema.contains("parent_id  BIGINT REFERENCES comments (id) ON DELETE CASCADE"));
        assertTrue(schema.contains("bookmarks_user_id_post_id_unique"));
        assertTrue(schema.contains("posts_slug_unique"));
        assertTrue(schema.contains("users_email_unique"));
    }

    @Test
    void schemaIsIdempotentAndNeverDropsData() throws IOException {
        String schema = read().toLowerCase();

        assertTrue(schema.contains("create table if not exists"));
        assertFalse(schema.contains("drop table"));
        assertFalse(schema.contains("truncate"));
    }

    private static String read() throws IOException {
        return Files.readString(SCHEMA, StandardCharsets.UTF_8);
    }

    /** SQL comments explain the exclusions, so only executable statements count. */
    private static String readWithoutComments() throws IOException {
        return read().replaceAll("(?m)--.*$", "");
    }
}
