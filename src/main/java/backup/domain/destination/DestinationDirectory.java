package backup.domain.destination;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DestinationDirectory {
    private final Path dir;
    public DestinationDirectory(Path dir) {
        this.dir = dir;
    }

    public void backupTo(Path originFile, Path relativePath) {
        final Path destinationFile = dir.resolve(relativePath);
        try {
            Files.createDirectories(destinationFile.getParent());
            Files.copy(originFile, destinationFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
