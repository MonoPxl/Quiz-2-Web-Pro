package id.myitsbroadcast.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfTest {

    @Test
    void acceptsOnlyTheExactToken() {
        assertTrue(Csrf.matches("abc123", "abc123"));
    }

    @Test
    void rejectsDifferentOrMissingTokens() {
        assertFalse(Csrf.matches("abc123", "abc124"));
        assertFalse(Csrf.matches("abc123", ""));
        assertFalse(Csrf.matches("abc123", null));
        assertFalse(Csrf.matches(null, "abc123"));
    }
}
