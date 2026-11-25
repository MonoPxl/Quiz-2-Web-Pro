package id.myitsbroadcast.model;

import java.util.Arrays;

public enum PostStatus {
    DRAFT("draft", "Draft"),
    PUBLISHED("published", "Published"),
    ARCHIVED("archived", "Archived");

    private final String value;
    private final String label;

    PostStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static PostStatus fromValue(String value) {
        if (value == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(status -> status.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
