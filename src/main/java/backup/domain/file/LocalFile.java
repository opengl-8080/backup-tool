package backup.domain.file;

import backup.domain.measure.StopWatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class LocalFile {
    private static final Pattern BACKUP_FILE_NAME_PATTERN = Pattern.compile("^.*#\\d{8}-\\d{9}$");
    private final Path path;

    public static LocalFile of(Path path) {
        return new LocalFile(path);
    }

    public LocalFile(Path path) {
        this.path = Objects.requireNonNull(path);
    }

    public LocalDirectory parent() {
        return LocalDirectory.of(path.getParent());
    }

    public boolean exists() {
        return StopWatch.measure("exists", () -> path.toFile().exists());
    }

    public void moveTo(LocalFile destination) {
        StopWatch.measure("moveTo", () -> {
            Files.move(path, destination.path);
        });
    }

    public void copyTo(LocalFile destination) {
        StopWatch.measure("copyTo", () -> {
            destination.parent().createDirectories();
            Files.copy(path, destination.path, StandardCopyOption.COPY_ATTRIBUTES);
        });
    }

    public LocalFile sibling(String fileName) {
        final Path path = this.path.getParent().resolve(fileName);
        return new LocalFile(path);
    }

    public String baseName() {
        return hasExtension() ? fileName().substring(0, lastDotIndex()) : fileName();
    }

    public Optional<String> extension() {
        if (hasExtension()) {
            return Optional.of(fileName().substring(lastDotIndex()));
        } else {
            return Optional.empty();
        }
    }

    private boolean hasExtension() {
        return lastDotIndex() != -1;
    }

    private int lastDotIndex() {
        final String fileName = fileName();
        return fileName.lastIndexOf(".");
    }

    private String fileName() {
        return path.getFileName().toString();
    }

    public boolean contentEquals(LocalFile other) {
        if (!exists() || !other.exists()) {
            return false;
        }

        if (size() != other.size()) {
            return false;
        }

        if (path.toFile().lastModified() == other.path.toFile().lastModified()) {
            return true;
        }

        try {
            return Files.mismatch(path, other.path) == -1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path path() {
        return path;
    }

    public boolean isLatest() {
        return !BACKUP_FILE_NAME_PATTERN.matcher(baseName()).matches();
    }

    private long size() {
        return StopWatch.measure("size", () -> path.toFile().length());
    }

    @Override
    public String toString() {
        return "LocalFile{" +
                "path=" + path +
                '}';
    }
}
