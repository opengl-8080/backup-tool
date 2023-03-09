package backup.domain.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LocalFile resolveFile(Path filePath) {
        return LocalFile.of(path.resolve(filePath));
    }
}
