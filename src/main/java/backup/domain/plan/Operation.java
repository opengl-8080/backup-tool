package backup.domain.plan;

import backup.domain.file.LocalFile;
import backup.domain.logging.Logger;
import backup.domain.time.SystemTime;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public enum Operation {
    ADD {
        @Override
        public void execute(Logger logger, LocalFile originFile, LocalFile destinationFile) {
            final long begin = System.currentTimeMillis();
            try {
                originFile.copyTo(destinationFile);
            } finally {
                final long time = System.currentTimeMillis() - begin;
                logger.infoFileOnly(
                        "ADD (%dms) origin=%s, dest=%s".formatted(time, originFile.path(), destinationFile.path()));
            }
        }
    },
    UPDATE {
        @Override
        public void execute(Logger logger, LocalFile originFile, LocalFile destinationFile) {
            final long begin = System.currentTimeMillis();
            Path rotatedPath = null;
            try {
                rotatedPath = rotate(destinationFile);
                originFile.copyTo(destinationFile);
            } finally {
                final long time = System.currentTimeMillis() - begin;
                logger.infoFileOnly(
                        "UPDATE (%dms) origin=%s, dest=%s, rotated=%s".formatted(time, originFile.path(), destinationFile.path(), rotatedPath));
            }
        }
    },
    REMOVE {
        @Override
        public void execute(Logger logger, LocalFile originFile, LocalFile destinationFile) {
            final long begin = System.currentTimeMillis();
            Path rotatedPath = null;
            try {
                rotatedPath = rotate(destinationFile);
            } finally {
                final long time = System.currentTimeMillis() - begin;
                logger.infoFileOnly(
                        "REMOVE (%dms) origin=%s, dest=%s, rotated=%s".formatted(time, originFile.path(), destinationFile.path(), rotatedPath));
            }
        }
    };
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmssSSS");

    public abstract void execute(Logger logger, LocalFile originFile, LocalFile destinationFile);

    protected Path rotate(LocalFile file) {
        final String baseName = file.baseName();
        final String extension = file.extension().orElse("");
        final String rotatedFileName = baseName + "#" + FORMATTER.format(SystemTime.now()) + extension;
        final LocalFile rotatedFile = file.sibling(rotatedFileName);
        file.moveTo(rotatedFile);
        return rotatedFile.path();
    }
}
