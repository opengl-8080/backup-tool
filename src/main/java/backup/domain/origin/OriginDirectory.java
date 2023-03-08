package backup.domain.origin;

import backup.domain.destination.DestinationDirectory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class OriginDirectory {
    private final Path dir;
    public OriginDirectory(Path dir) {
        this.dir = dir;
    }

    public void backupTo(DestinationDirectory destination) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final Path relativePath = dir.relativize(file);
                    destination.backupTo(file, relativePath);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
