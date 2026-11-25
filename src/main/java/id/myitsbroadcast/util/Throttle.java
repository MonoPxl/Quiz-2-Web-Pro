package id.myitsbroadcast.util;

import id.myitsbroadcast.config.AppConfig;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple sliding-window throttle for login attempts, replacing Fortify's
 * per-email-and-IP limiter. State is per application instance, which is the
 * same durability a single-node deployment needs.
 */
public final class Throttle {

    private static final Map<String, Deque<Long>> ATTEMPTS = new ConcurrentHashMap<>();

    private Throttle() {
    }

    private static int maxAttempts() {
        return AppConfig.integer("LOGIN_MAX_ATTEMPTS", 5);
    }

    private static long windowMillis() {
        return AppConfig.integer("LOGIN_WINDOW_SECONDS", 60) * 1000L;
    }

    public static boolean tooMany(String key) {
        Deque<Long> attempts = ATTEMPTS.get(key);

        if (attempts == null) {
            return false;
        }

        synchronized (attempts) {
            prune(attempts);
            return attempts.size() >= maxAttempts();
        }
    }

    public static void hit(String key) {
        Deque<Long> attempts = ATTEMPTS.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (attempts) {
            prune(attempts);
            attempts.addLast(System.currentTimeMillis());
        }
    }

    public static void clear(String key) {
        ATTEMPTS.remove(key);
    }

    public static void resetAll() {
        ATTEMPTS.clear();
    }

    private static void prune(Deque<Long> attempts) {
        long threshold = System.currentTimeMillis() - windowMillis();

        while (!attempts.isEmpty() && attempts.peekFirst() < threshold) {
            attempts.removeFirst();
        }
    }
}
