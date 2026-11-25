package id.myitsbroadcast.util;

import id.myitsbroadcast.config.AppConfig;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Safe storage for user-supplied images.
 *
 * <p>Files are validated by magic bytes rather than the client-supplied
 * content type, renamed with a random UUID, and written beneath the configured
 * storage directory. Nothing outside that directory can be read or deleted.</p>
 */
public final class Uploads {

    public static final long MAX_BYTES = 2L * 1024 * 1024;
    public static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private Uploads() {
    }

    public static class InvalidUploadException extends Exception {
        public InvalidUploadException(String message) {
            super(message);
        }
    }

    public static boolean isPresent(Part part) {
        return part != null && part.getSize() != 0 && part.getSubmittedFileName() != null
                && !part.getSubmittedFileName().isBlank();
    }

    /** Validate without persisting, so form errors can be shown before any write. */
    public static void validate(Part part) throws InvalidUploadException {
        if (!isPresent(part)) {
            return;
        }

        if (part.getSize() > MAX_BYTES) {
            throw new InvalidUploadException("The image must not be larger than 2 MB.");
        }

        String extension = extensionOf(part.getSubmittedFileName());

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidUploadException("The image must be a JPG, PNG or WebP file.");
        }

        try (InputStream input = part.getInputStream()) {
            if (sniff(input.readNBytes(12)) == null) {
                throw new InvalidUploadException("The uploaded file is not a valid image.");
            }
        } catch (IOException exception) {
            throw new InvalidUploadException("The uploaded image could not be read.");
        }
    }

    /**
     * Store the upload and return its storage-relative path, for example
     * {@code avatars/6f1c....png}. Returns {@code null} when no file was sent.
     */
    public static String store(Part part, String directory) throws InvalidUploadException, IOException {
        if (!isPresent(part)) {
            return null;
        }

        validate(part);

        String extension;
        try (InputStream input = part.getInputStream()) {
            extension = sniff(input.readNBytes(12));
        }

        if (extension == null) {
            throw new InvalidUploadException("The uploaded file is not a valid image.");
        }

        Path targetDirectory = AppConfig.storageDir().resolve(directory).normalize();
        Files.createDirectories(targetDirectory);

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = targetDirectory.resolve(filename);

        try (InputStream input = part.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return directory + "/" + filename;
    }

    public static void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException | IllegalArgumentException ignored) {
            // A missing or malformed legacy path must never break a request.
        }
    }

    public static Path resolve(String relativePath) {
        Path base = AppConfig.storageDir().toAbsolutePath().normalize();
        Path target = base.resolve(relativePath).normalize();

        if (!target.startsWith(base)) {
            throw new IllegalArgumentException("Invalid storage path.");
        }

        return target;
    }

    public static String contentType(String relativePath) {
        String extension = extensionOf(relativePath);

        return switch (extension) {
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "jpg", "jpeg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }

    public static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }

        int dot = filename.lastIndexOf('.');

        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** Return the canonical extension for a supported image, or null. */
    private static String sniff(byte[] header) {
        if (header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF) {
            return "jpg";
        }

        if (header.length >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
                && header[4] == 0x0D && header[5] == 0x0A && header[6] == 0x1A && header[7] == 0x0A) {
            return "png";
        }

        if (header.length >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "webp";
        }

        return null;
    }
}
