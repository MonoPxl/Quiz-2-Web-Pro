package id.myitsbroadcast.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordsTest {

    @AfterEach
    void clearEnvironmentOverrides() {
        System.clearProperty("APP_ENV");
    }

    @Test
    void hashesWithBCryptAndVerifies() {
        String hash = Passwords.hash("campus-signal-2026");

        assertTrue(hash.startsWith("$2a$"));
        assertTrue(Passwords.check("campus-signal-2026", hash));
        assertFalse(Passwords.check("campus-signal-2025", hash));
    }

    @Test
    void verifiesLegacyLaravelDollar2yHashes() {
        String javaHash = BCrypt.hashpw("password", BCrypt.gensalt(4));
        String laravelHash = "$2y$" + javaHash.substring(4);

        assertTrue(Passwords.check("password", laravelHash));
        assertFalse(Passwords.check("not-password", laravelHash));
    }

    @Test
    void rejectsMissingAndMalformedHashes() {
        assertFalse(Passwords.check(null, "$2a$12$abcdef"));
        assertFalse(Passwords.check("password", null));
        assertFalse(Passwords.check("password", "   "));
        assertFalse(Passwords.check("password", "plaintext"));
    }

    @Test
    void developmentPolicyRequiresEightCharactersAndConfirmation() {
        System.setProperty("APP_ENV", "development");

        assertEquals(List.of("The password must be at least 8 characters."),
                Passwords.validate("short", "short"));
        assertEquals(List.of("The password confirmation does not match."),
                Passwords.validate("longenough", "different"));
        assertTrue(Passwords.validate("longenough", "longenough").isEmpty());
        assertEquals(List.of("The password field is required."), Passwords.validate("", ""));
    }

    @Test
    void productionPolicyRequiresLengthCaseNumberAndSymbol() {
        System.setProperty("APP_ENV", "production");

        List<String> errors = Passwords.validate("weakpass", "weakpass");

        assertTrue(errors.stream().anyMatch(message -> message.contains("at least 12")));
        assertTrue(errors.stream().anyMatch(message -> message.contains("uppercase")));
        assertTrue(errors.stream().anyMatch(message -> message.contains("number")));
        assertTrue(errors.stream().anyMatch(message -> message.contains("symbol")));
        assertTrue(Passwords.validate("Campus-Signal-2026", "Campus-Signal-2026").isEmpty());
    }
}
