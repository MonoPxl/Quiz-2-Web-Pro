package id.myitsbroadcast.util;

import java.text.Normalizer;
import java.util.Locale;

/** URL slug generation matching Laravel's {@code Str::slug} behaviour closely. */
public final class Slug {

    private Slug() {
    }

    public static String slugify(String value) {
        if (value == null || value.isBlank()) {
            return "post";
        }

        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        String slug = decomposed.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (slug.length() > 180) {
            slug = slug.substring(0, 180).replaceAll("-+$", "");
        }

        return slug.isEmpty() ? "post" : slug;
    }

    /** Append a numeric suffix until the candidate is free. */
    public static String unique(String base, java.util.function.Predicate<String> exists) {
        String candidate = slugify(base);
        String stem = candidate;
        int suffix = 2;

        while (exists.test(candidate)) {
            candidate = stem + "-" + suffix;
            suffix++;
        }

        return candidate;
    }
}
