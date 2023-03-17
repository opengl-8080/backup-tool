package backup.domain.cache;

import backup.domain.file.LocalFile;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DestinationCacheDatabase {
    private static final DestinationCacheDatabase INSTANCE = new DestinationCacheDatabase();

    public static DestinationCacheDatabase getInstance() {
        return INSTANCE;
    }
    private final Map<Path, byte[]> caches = new ConcurrentHashMap<>();

    public void put(Path path, byte[] hash) {
        caches.put(path, hash);
    }

    public Optional<byte[]> getHash(Path path) {
        return Optional.ofNullable(caches.get(path));
    }

    public void reset() {
        caches.clear();
    }

    private DestinationCacheDatabase() {}
}
