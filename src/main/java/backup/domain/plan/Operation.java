package backup.domain.plan;

import backup.domain.file.LocalFile;
import backup.domain.time.SystemTime;

import java.time.format.DateTimeFormatter;

public enum Operation {
    ADD {
        @Override
        public void execute(LocalFile originFile, LocalFile destinationFile) {
            originFile.copyTo(destinationFile);
        }
    },
    UPDATE {
        @Override
        public void execute(LocalFile originFile, LocalFile destinationFile) {
            rotate(destinationFile);
            originFile.copyTo(destinationFile);
        }
    },
    REMOVE {
        @Override
        public void execute(LocalFile originFile, LocalFile destinationFile) {
            rotate(destinationFile);
        }
    };
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmssSSS");

    public abstract void execute(LocalFile originFile, LocalFile destinationFile);

    protected void rotate(LocalFile file) {
        final String baseName = file.baseName();
        final String extension = file.extension();
        final String rotatedFileName = baseName + "#" + FORMATTER.format(SystemTime.now()) + extension;
        final LocalFile rotatedFile = file.sibling(rotatedFileName);
        file.moveTo(rotatedFile);
    }
}
