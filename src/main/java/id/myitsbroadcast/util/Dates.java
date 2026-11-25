package id.myitsbroadcast.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Date helpers. All user-facing times are rendered in Asia/Jakarta, matching
 * the source application's configured timezone.
 */
public class Dates {

    public static final ZoneId ZONE = ZoneId.of("Asia/Jakarta");

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("d MMM uuuu, HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter LONG_DATE =
            DateTimeFormatter.ofPattern("EEEE, d MMMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter INPUT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm", Locale.ENGLISH);

    public LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    public String date(LocalDateTime value) {
        return value == null ? "" : DATE.format(value);
    }

    public String time(LocalDateTime value) {
        return value == null ? "" : TIME.format(value);
    }

    public String dateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME.format(value);
    }

    public String longDate(LocalDateTime value) {
        return value == null ? "" : LONG_DATE.format(value);
    }

    /** Value for an {@code <input type="datetime-local">}. */
    public String input(LocalDateTime value) {
        return value == null ? "" : INPUT.format(value);
    }

    public long daysUntil(LocalDateTime value) {
        if (value == null) {
            return 0;
        }

        return java.time.Duration.between(now(), value).toDays();
    }
}
