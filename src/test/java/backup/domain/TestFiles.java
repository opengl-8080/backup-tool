package backup.domain;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class TestFiles implements BeforeEachCallback {
    private final Path tempDir = Path.of("build/test-temp");
    private final Path originDir = tempDir.resolve("origin");
    private final Path destinationDir = tempDir.resolve("destination");

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        reset();
    }

    public void reset() {
        rmDir(tempDir);

        mkdirs(originDir);
        mkdirs(destinationDir);
    }

    public Path originDir() {
        return originDir;
    }

    public Path destinationDir() {
        return destinationDir;
    }

    public void removeOriginFile(String path) {
        try {
            Files.delete(originFile(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path writeOriginFile(String path, String content) {
        final Path file = originFile(path);
        writeFile(file, content, null);
        return file;
    }

    public Path writeOriginFile(String path, String content, long lastModified) {
        final Path file = originFile(path);
        writeFile(file, content, lastModified);
        return file;
    }

    public Path writeDestinationFile(String path, String content) {
        final Path file = destinationFile(path);
        writeFile(file, content, null);
        return file;
    }

    public Path writeDestinationFile(String path, String content, long lastModified) {
        final Path file = destinationFile(path);
        writeFile(file, content, lastModified);
        return file;
    }

    private void writeFile(Path file, String content, Long lastModified) {
        mkdirs(file.getParent());
        try {
            Files.writeString(file, content, StandardCharsets.UTF_8);
            if (lastModified != null) {
                file.toFile().setLastModified(lastModified);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path originFile(String path) {
        return originDir.resolve(path);
    }

    public Path destinationFile(String path) {
        return destinationDir.resolve(path);
    }

    public Path destinationDir(String path) {
        return destinationFile(path);
    }

    private void mkdirs(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void rmDir(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
