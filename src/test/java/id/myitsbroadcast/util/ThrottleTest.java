package id.myitsbroadcast.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThrottleTest {

    @Test
    void blocksAfterFiveAttemptsForTheSameKey() {
        String key = "login:" + UUID.randomUUID();

        for (int attempt = 0; attempt < 5; attempt++) {
            assertFalse(Throttle.tooMany(key));
            Throttle.hit(key);
        }

        assertTrue(Throttle.tooMany(key));
    }

    @Test
    void separateEmailAndAddressKeysDoNotShareState() {
        String first = "login:" + UUID.randomUUID();
        String second = "login:" + UUID.randomUUID();

        for (int attempt = 0; attempt < 5; attempt++) {
            Throttle.hit(first);
        }

        assertTrue(Throttle.tooMany(first));
        assertFalse(Throttle.tooMany(second));
    }
}
