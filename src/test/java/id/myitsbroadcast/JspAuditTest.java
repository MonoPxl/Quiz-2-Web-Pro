package id.myitsbroadcast;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Static checks over the JSP view layer: views must stay scriptlet-free, hold
 * no SQL, use the Jakarta taglib URIs, keep logout out of shared navigation and
 * render only the wordmark in the footer.
 */
class JspAuditTest {

    private static final Path VIEWS = Path.of("src/main/webapp/WEB-INF/views");
    private static final Path TAGS = Path.of("src/main/webapp/WEB-INF/tags");

    private static final Pattern SCRIPTLET = Pattern.compile("<%(?!@|--)");
    private static final Pattern SQL = Pattern.compile(
            "(?i)\\b(select\\s+[^<]{0,200}\\s+from|insert\\s+into|update\\s+\\w+\\s+set|delete\\s+from)\\b");

    @Test
    void viewsContainNoScriptletsOrExpressions() throws IOException {
        for (Path file : jspFiles()) {
            String source = Files.readString(file, StandardCharsets.UTF_8);

            assertFalse(SCRIPTLET.matcher(source).find(),
                    "scriptlet or expression found in " + file);
        }
    }

    @Test
    void viewsContainNoSqlOrJdbcImports() throws IOException {
        for (Path file : jspFiles()) {
            String source = Files.readString(file, StandardCharsets.UTF_8);

            assertFalse(SQL.matcher(source).find(), "SQL found in " + file);
            assertFalse(source.contains("java.sql"), "JDBC import found in " + file);
            assertFalse(source.contains("javax.servlet"), "javax namespace found in " + file);
        }
    }

    @Test
    void viewsUseJakartaJstlUris() throws IOException {
        for (Path file : jspFiles()) {
            String source = Files.readString(file, StandardCharsets.UTF_8);

            assertFalse(source.contains("http://java.sun.com/jsp/jstl"),
                    "legacy JSTL URI found in " + file);
        }
    }

    @Test
    void sharedNavigationHasNoLogoutControl() throws IOException {
        for (String name : List.of("partials/head.jsp", "partials/nav-links.jsp",
                "partials/account-controls.jsp")) {
            String source = Files.readString(VIEWS.resolve(name), StandardCharsets.UTF_8);

            assertFalse(source.toLowerCase(Locale.ROOT).contains("logout"),
                    "logout must stay out of shared navigation: " + name);
        }
    }

    @Test
    void footerRendersOnlyTheWordmark() throws IOException {
        String source = Files.readString(VIEWS.resolve("partials/foot.jsp"), StandardCharsets.UTF_8);
        String visible = source
                .replaceAll("(?s)<%--.*?--%>", " ")
                .replaceAll("(?s)<%@.*?%>", " ")
                .replaceAll("(?s)<script.*?</script>", " ")
                .replaceAll("<[^>]*>", " ")
                .replaceAll("\\s+", " ")
                .trim();

        assertEquals("myITS Broadcast", visible);
    }

    @Test
    void noEmailVerificationPageExists() throws IOException {
        List<Path> files = jspFiles();

        for (Path file : files) {
            String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
            assertFalse(name.contains("verify") || name.contains("verification"),
                    "verification page must not exist: " + file);
        }

        assertFalse(Files.exists(VIEWS.resolve("auth/verify-email.jsp")));
    }

    private static List<Path> jspFiles() throws IOException {
        List<Path> files = new ArrayList<>();

        for (Path root : List.of(VIEWS, TAGS)) {
            if (!Files.isDirectory(root)) {
                continue;
            }

            try (Stream<Path> stream = Files.walk(root)) {
                stream.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".jsp")
                                || path.toString().endsWith(".tag"))
                        .forEach(files::add);
            }
        }

        assertTrue(files.size() >= 15, "expected the full view set, found " + files.size());

        return files;
    }
}
