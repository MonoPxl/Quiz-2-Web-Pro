package id.myitsbroadcast.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageTest {

    @Test
    void computesPageCountFromTotal() {
        Page<String> page = new Page<>(List.of("a"), 15, 1, 9);

        assertEquals(2, page.getTotalPages());
        assertEquals(1, page.getFrom());
        assertEquals(9, page.getTo());
        assertFalse(page.isHasPrevious());
        assertTrue(page.isHasNext());
    }

    @Test
    void clampsEdgesOnTheLastPage() {
        Page<String> page = new Page<>(List.of("a", "b"), 20, 3, 9);

        assertEquals(3, page.getTotalPages());
        assertEquals(19, page.getFrom());
        assertEquals(20, page.getTo());
        assertTrue(page.isHasPrevious());
        assertFalse(page.isHasNext());
        assertEquals(2, page.getPreviousPage());
        assertEquals(3, page.getNextPage());
    }

    @Test
    void emptyResultStillHasOnePage() {
        Page<String> page = new Page<>(List.of(), 0, 1, 9);

        assertEquals(1, page.getTotalPages());
        assertEquals(0, page.getFrom());
        assertEquals(0, page.getTo());
        assertTrue(page.isEmpty());
    }

    @Test
    void guardsAgainstZeroPageSize() {
        Page<String> page = new Page<>(List.of(), 0, 1, 0);

        assertEquals(1, page.getTotalPages());
        assertFalse(page.isHasNext());
    }
}
