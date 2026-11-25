package id.myitsbroadcast.model;

import java.util.Arrays;

public enum PostType {
    ANNOUNCEMENT("announcement", "Announcement"),
    EVENT("event", "Event");

    private final String value;
    private final String label;

    PostType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static PostType fromValue(String value) {
        if (value == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
