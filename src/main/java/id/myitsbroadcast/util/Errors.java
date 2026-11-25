package id.myitsbroadcast.util;

import java.util.LinkedHashMap;

/**
 * Field-keyed validation messages.
 *
 * <p>Extends {@link LinkedHashMap} so JSP EL can read messages directly with
 * {@code ${errors.title}} while the {@code has} helper keeps templates terse.</p>
 */
public class Errors extends LinkedHashMap<String, String> {

    public void add(String field, String message) {
        putIfAbsent(field, message);
    }

    public boolean has(String field) {
        return containsKey(field);
    }

    public boolean any() {
        return !isEmpty();
    }
}
