package backup.domain;

import backup.domain.file.LocalDirectory;
import backup.domain.file.LocalFile;
import backup.domain.time.SystemTime;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class BackupService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmssSSS");
    private final LocalDirectory originDirectory;
    private final LocalDirectory destinationDirectory;

    public BackupService(LocalDirectory originDirectory, LocalDirectory destinationDirectory) {
        this.originDirectory = Objects.requireNonNull(originDirectory);
        this.destinationDirectory = Objects.requireNonNull(destinationDirectory);
    }

    public void backup() {
        originDirectory.walk((originFile, relativePath) -> {
            final LocalFile destinationFile = destinationDirectory.resolveFile(relativePath);
            destinationFile.parent().createDirectories();

            if (destinationFile.exists() && !originFile.contentEquals(destinationFile)) {
                rotate(destinationFile);
            }

            originFile.copyTo(destinationFile);
        });

        destinationDirectory.walk((destinationFile, relativePath) -> {
            if (destinationFile.isLatest() && !originDirectory.resolveFile(relativePath).exists()) {
                rotate(destinationFile);
            }
        });
    }

    private void rotate(LocalFile destinationFile) {
        final String baseName = destinationFile.baseName();
        final String extension = destinationFile.extension();
        final String rotatedFileName = baseName + "#" + FORMATTER.format(SystemTime.now()) + extension;
        final LocalFile rotatedFile = destinationFile.sibling(rotatedFileName);
        destinationFile.moveTo(rotatedFile);
    }
}