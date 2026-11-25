package id.myitsbroadcast;

import org.apache.catalina.startup.Tomcat;
import org.apache.jasper.JspC;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deployment smoke test. It assembles the real webapp layout (JSPs, classes,
 * runtime jars), precompiles every JSP with Jasper, then boots embedded Tomcat
 * 10.1 and checks the endpoints that work without a database.
 *
 * <p>Database-backed pages are deliberately not requested here: this project
 * must not connect to a PostgreSQL instance during the build.</p>
 */
class WebAppSmokeTest {

    private static Path webapp;
    private static Tomcat tomcat;
    private static String baseUrl;

    @BeforeAll
    static void assembleAndStart() throws Exception {
        webapp = assemble();

        tomcat = new Tomcat();
        tomcat.setBaseDir(Files.createTempDirectory("myitsbroadcast-tomcat").toString());
        tomcat.setPort(0);
        tomcat.getConnector();
        tomcat.addWebapp("", webapp.toAbsolutePath().toString());
        tomcat.start();

        baseUrl = "http://localhost:" + tomcat.getConnector().getLocalPort();
    }

    @AfterAll
    static void stop() throws Exception {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    @Test
    void everyJspCompiles() throws Exception {
        Path output = Path.of("target", "jspc");
        deleteTree(output);
        Files.createDirectories(output);

        JspC jspc = new JspC();
        jspc.setUriroot(webapp.toAbsolutePath().toString());
        jspc.setOutputDir(output.toAbsolutePath().toString());
        jspc.setCompile(true);
        jspc.setFailOnError(true);
        jspc.setClassPath(System.getProperty("surefire.test.class.path",
                System.getProperty("java.class.path")));
        jspc.execute();

        try (Stream<Path> compiled = Files.walk(output)) {
            long classes = compiled.filter(path -> path.toString().endsWith(".class")).count();
            assertTrue(classes >= 15, "expected compiled JSP classes, found " + classes);
        }
    }

    @Test
    void healthEndpointRespondsWithoutADatabase() throws Exception {
        HttpResponse<String> response = get("/health");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\":\"ok\""));
        assertTrue(response.body().contains("\"application\":\"myitsbroadcast\""));
    }

    @Test
    void stylesheetIsServed() throws Exception {
        HttpResponse<String> response = get("/assets/css/app.css");

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("content-type").orElse("").contains("text/css"));
        assertTrue(response.body().contains("--blue-700"));
    }

    @Test
    void unknownUrlRendersTheJspErrorPage() throws Exception {
        HttpResponse<String> response = get("/this-page-does-not-exist");

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("This signal is off the air"));
        assertTrue(response.body().contains("myITS Broadcast"));
        assertTrue(response.body().contains("site-footer"));
    }

    private static HttpResponse<String> get(String path) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static Path assemble() throws IOException {
        Path target = Path.of("target", "test-webapp");
        deleteTree(target);

        copyTree(Path.of("src", "main", "webapp"), target);
        copyTree(Path.of("target", "classes"), target.resolve("WEB-INF").resolve("classes"));

        Path lib = Files.createDirectories(target.resolve("WEB-INF").resolve("lib"));
        String classpath = System.getProperty("surefire.test.class.path",
                System.getProperty("java.class.path"));

        for (String entry : classpath.split(File.pathSeparator)) {
            Path jar = Path.of(entry);

            if (!Files.isRegularFile(jar) || !entry.toLowerCase().endsWith(".jar")) {
                continue;
            }

            String name = jar.getFileName().toString().toLowerCase();

            if (name.startsWith("jakarta.servlet-api") || name.startsWith("jakarta.servlet.jsp-api")
                    || name.startsWith("jakarta.el-api") || name.startsWith("tomcat-")
                    || name.startsWith("junit-") || name.startsWith("opentest4j")
                    || name.startsWith("apiguardian") || name.startsWith("surefire")) {
                continue;
            }

            Files.copy(jar, lib.resolve(jar.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }

        return target;
    }

    private static void copyTree(Path source, Path destination) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            for (Path path : stream.toList()) {
                Path target = destination.resolve(source.relativize(path));

                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }

        try (Stream<Path> stream = Files.walk(root)) {
            for (Path path : stream.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
