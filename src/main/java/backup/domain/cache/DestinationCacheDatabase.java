package backup.domain.cache;

import backup.domain.measure.StopWatch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File Format.
 * <p>
 * | Path Size (4 byte) | Path Content | Hash Size (4 byte) | Hash Content |...
 * </p>
 */
public class DestinationCacheDatabase {
    private final int BUFFER_SIZE = 1024 * 1024;
    private final Map<Path, byte[]> caches = new ConcurrentHashMap<>();
    private final Path persistenceFile;

    public DestinationCacheDatabase(Path persistenceFile) {
        this.persistenceFile = Objects.requireNonNull(persistenceFile);
    }

    public void put(Path path, byte[] hash) {
        caches.put(path, hash);
    }

    public void remove(Path path) {
        caches.remove(path);
    }

    public Optional<byte[]> getHash(Path path) {
        return Optional.ofNullable(caches.get(path));
    }

    public void reset() {
        caches.clear();
    }

    public void restoreFromFile() {
        StopWatch.measure("restoreFormFile", () -> {
            reset();

            if (!persistenceFile.toFile().exists()) {
                return;
            }

            try (
                final InputStream in = new BufferedInputStream(Files.newInputStream(persistenceFile, StandardOpenOption.READ), BUFFER_SIZE);
            ) {
                int pathSize;
                while ((pathSize = readNextInt(in)) != -1) {
                    final Path path = Path.of(new String(readNextBlock(in, pathSize), StandardCharsets.UTF_8));
                    final byte[] hash = readNextBlock(in, readNextInt(in));

                    put(path, hash);
                }
            }
        });
    }

    public void saveToFile() {
        StopWatch.measure("saveToFile", () -> {
            Files.createDirectories(persistenceFile.getParent());

            try (
                final OutputStream out = new BufferedOutputStream(Files.newOutputStream(persistenceFile), BUFFER_SIZE);
            ) {
                for (Map.Entry<Path, byte[]> entry : caches.entrySet()) {
                    final Path path = entry.getKey();
                    final byte[] pathBytes = path.toString().getBytes(StandardCharsets.UTF_8);
                    out.write(intToByteArray(pathBytes.length));
                    out.write(pathBytes);

                    final byte[] hash = entry.getValue();
                    out.write(intToByteArray(hash.length));
                    out.write(hash);
                }
            }
        });
    }

    private byte[] intToByteArray(int i) {
        final ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(i);
        return buffer.array();
    }

    private int readNextInt(InputStream in) throws IOException {
        byte[] bytes = new byte[4];
        if (in.read(bytes) != 4) {
            return -1;
        }
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getInt();
    }

    private byte[] readNextBlock(InputStream in, int size) throws IOException {
        byte[] buffer = new byte[size];
        final int actualSize = in.read(buffer);
        if (actualSize != size) {
            throw new RuntimeException(String.format("expected size is %d but actual is %d", size, actualSize));
        }
        return buffer;
    }
}
