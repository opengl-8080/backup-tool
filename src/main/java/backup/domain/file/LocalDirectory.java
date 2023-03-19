package backup.domain.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class LocalDirectory {
    private final Path path;

    public static LocalDirectory of(Path path) {
        return new LocalDirectory(path);
    }

    public LocalDirectory(Path path) {
        this.path = Objects.requireNonNull(path);
    }

    public void createDirectories() {
        if (Files.exists(path)) {
            return;
        }
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LocalFile resolveFile(Path filePath) {
        return LocalFile.of(path.resolve(filePath));
    }

    public void walk(DirectoryVisitor visitor) {
        if (!path.toFile().exists()) {
            return;
        }

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final Path relativePath = path.relativize(file);
                    visitor.visit(LocalFile.of(file), relativePath);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path path() {
        return path;
    }

    @Override
    public String toString() {
        return "LocalDirectory{" +
                "path=" + path +
                '}';
    }
}
