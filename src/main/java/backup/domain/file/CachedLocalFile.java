package backup.domain.file;

import backup.domain.cache.DestinationCacheDatabase;

import java.util.Objects;

public class CachedLocalFile extends LocalFile {
    private final DestinationCacheDatabase cache;

    public CachedLocalFile(DestinationCacheDatabase cache, LocalFile delegate) {
        super(delegate.path());
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public byte[] hash() {
        return cache.getHash(path()).orElseGet(super::hash);
    }
}
