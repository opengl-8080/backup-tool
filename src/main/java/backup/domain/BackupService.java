package backup.domain;

import backup.domain.file.LocalDirectory;
import backup.domain.file.LocalFile;
import backup.domain.measure.StopWatch;
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
        final StopWatch backup = StopWatch.start("backup");
        backupUpdatedFiles();
        backupRemovedFiles();
        backup.stop();
    }

    private void backupUpdatedFiles() {
        final StopWatch stopWatch = StopWatch.start("backupUpdatedFiles");
        originDirectory.walk((originFile, relativePath) -> {
            final StopWatch stopWatch2 = StopWatch.start("before copyTo");
            final LocalFile destinationFile = destinationDirectory.resolveFile(relativePath);

            if (originFile.contentEquals(destinationFile)) {
                return;
            }

            if (destinationFile.exists()) {
                rotate(destinationFile);
            }

            stopWatch2.stop();
            originFile.copyTo(destinationFile);
        });
        stopWatch.stop();
    }

    private void backupRemovedFiles() {
        final StopWatch stopWatch = StopWatch.start("backupRemovedFiles");
        destinationDirectory.walk((destinationFile, relativePath) -> {
            if (destinationFile.isLatest() && originDirectory.doesNotHave(relativePath)) {
                rotate(destinationFile);
            }
        });
        stopWatch.stop();
    }

    private void rotate(LocalFile destinationFile) {
        final String baseName = destinationFile.baseName();
        final String extension = destinationFile.extension();
        final String rotatedFileName = baseName + "#" + FORMATTER.format(SystemTime.now()) + extension;
        final LocalFile rotatedFile = destinationFile.sibling(rotatedFileName);
        destinationFile.moveTo(rotatedFile);
    }
}
