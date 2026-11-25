package id.myitsbroadcast.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatesTest {

    private final Dates dates = new Dates();
    private final LocalDateTime sample = LocalDateTime.of(2026, 11, 18, 9, 0);

    @Test
    void formatsJakartaFacingDates() {
        assertEquals("18 Nov 2026", dates.date(sample));
        assertEquals("09:00", dates.time(sample));
        assertEquals("18 Nov 2026, 09:00", dates.dateTime(sample));
        assertEquals("2026-11-18T09:00", dates.input(sample));
    }

    @Test
    void longDateSpellsTheMonth() {
        assertTrue(dates.longDate(sample).endsWith("18 November 2026"));
    }

    @Test
    void rendersNullAsAnEmptyString() {
        assertEquals("", dates.date(null));
        assertEquals("", dates.time(null));
        assertEquals("", dates.dateTime(null));
        assertEquals("", dates.longDate(null));
        assertEquals("", dates.input(null));
    }
}
