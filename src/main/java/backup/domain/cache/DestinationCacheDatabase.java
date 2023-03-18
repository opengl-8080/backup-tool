package backup.domain.cache;

import backup.domain.measure.StopWatch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File Format.
 * <p>
 * | Path Size (4 byte) | Path Content | Hash Size (4 byte) | Hash Content |...
 * </p>
 */
public class DestinationCacheDatabase {
    private static final DestinationCacheDatabase INSTANCE = new DestinationCacheDatabase();

    public static DestinationCacheDatabase getInstance() {
        return INSTANCE;
    }
    private final Map<Path, byte[]> caches = new ConcurrentHashMap<>();
    private Path persistenceFile;

    private DestinationCacheDatabase() {}

    public void put(Path path, byte[] hash) {
        caches.put(path, hash);
    }

    public Optional<byte[]> getHash(Path path) {
        return Optional.ofNullable(caches.get(path));
    }

    public void reset() {
        caches.clear();
    }

    public void setPersistenceFile(Path persistenceFile) {
        this.persistenceFile = persistenceFile;
    }
    private final int BUFFER_SIZE = 1024 * 1024;

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
                while ((pathSize = in.read()) != -1) {
                    final Path path = Path.of(new String(readNextBlock(in, pathSize), StandardCharsets.UTF_8));
                    final byte[] hash = readNextBlock(in, in.read());

                    put(path, hash);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void saveToFile() {
        StopWatch.measure("saveToFile", () -> {
            try (
                final OutputStream out = new BufferedOutputStream(Files.newOutputStream(persistenceFile), BUFFER_SIZE);
            ) {
                for (Map.Entry<Path, byte[]> entry : caches.entrySet()) {
                    final Path path = entry.getKey();
                    final byte[] pathBytes = path.toString().getBytes(StandardCharsets.UTF_8);
                    out.write(pathBytes.length);
                    out.write(pathBytes);

                    final byte[] hash = entry.getValue();
                    out.write(hash.length);
                    out.write(hash);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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
