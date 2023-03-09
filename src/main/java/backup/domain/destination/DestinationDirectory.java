package backup.domain.destination;

import backup.domain.file.LocalDirectory;
import backup.domain.file.LocalFile;
import backup.domain.time.SystemTime;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class DestinationDirectory {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmssSSS");
    private final LocalDirectory dir;
    public DestinationDirectory(Path dir) {
        this.dir = LocalDirectory.of(dir);
    }

    public void backupTo(LocalFile originFile, Path relativePath) {
        final LocalFile destinationFile = dir.resolveFile(relativePath);
        try {
            destinationFile.parent().createDirectories();

            if (destinationFile.exists() && !originFile.contentEquals(destinationFile)) {
                rotate(destinationFile);
            }

            originFile.copyTo(destinationFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void rotate(LocalFile destinationFile) throws IOException {
        final String baseName = destinationFile.baseName();
        final String extension = destinationFile.extension();
        final String rotatedFileName = baseName + "#" + FORMATTER.format(SystemTime.now()) + extension;
        final LocalFile rotatedFile = destinationFile.sibling(rotatedFileName);
        destinationFile.moveTo(rotatedFile);
    }
}
