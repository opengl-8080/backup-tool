package backup.domain.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
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
        return Files.exists(path);
    }

    public void moveTo(LocalFile destination) {
        try {
            Files.move(path, destination.path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyTo(LocalFile destination) {
        try {
            Files.copy(path, destination.path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LocalFile sibling(String fileName) {
        final Path path = this.path.getParent().resolve(fileName);
        return new LocalFile(path);
    }

    public String baseName() {
        return fileName().substring(0, lastDotIndex());
    }

    public String extension() {
        return fileName().substring(lastDotIndex());
    }

    private int lastDotIndex() {
        final String fileName = fileName();
        return fileName.lastIndexOf(".");
    }

    private String fileName() {
        return path.getFileName().toString();
    }

    public boolean contentEquals(LocalFile other) {
        return Arrays.equals(hash(), other.hash());
    }

    private byte[] hash() {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try (
            final BufferedInputStream in = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ));
            final DigestOutputStream out = new DigestOutputStream(OutputStream.nullOutputStream(), md)
        ) {
            in.transferTo(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return md.digest();
    }

    public Path path() {
        return path;
    }

    public boolean isLatest() {
        return !BACKUP_FILE_NAME_PATTERN.matcher(baseName()).matches();
    }

    @Override
    public String toString() {
        return "LocalFile{" +
                "path=" + path +
                '}';
    }
}
