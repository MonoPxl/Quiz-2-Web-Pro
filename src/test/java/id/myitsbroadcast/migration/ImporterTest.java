package id.myitsbroadcast.migration;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImporterTest {

    @Test
    void readsSqliteSpaceSeparatedTimestamps() {
        assertEquals(LocalDateTime.of(2026, 11, 18, 9, 30, 15),
                Importer.parse("2026-11-18 09:30:15"));
    }

    @Test
    void readsIsoTimestamps() {
        assertEquals(LocalDateTime.of(2026, 11, 18, 9, 30, 15),
                Importer.parse("2026-11-18T09:30:15"));
        assertEquals(LocalDateTime.of(2026, 11, 18, 9, 30),
                Importer.parse("2026-11-18T09:30"));
    }

    @Test
    void readsDateOnlyValuesAtNoon() {
        assertEquals(LocalDateTime.of(2026, 11, 18, 12, 0),
                Importer.parse("2026-11-18"));
    }

    @Test
    void treatsBlankAndInvalidValuesAsMissing() {
        assertNull(Importer.parse(null));
        assertNull(Importer.parse(""));
        assertNull(Importer.parse("   "));
        assertNull(Importer.parse("not-a-date"));
    }
}
