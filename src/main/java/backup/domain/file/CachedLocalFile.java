package backup.domain.file;

import backup.domain.cache.DestinationCacheDatabase;

public class CachedLocalFile extends LocalFile {
    public CachedLocalFile(LocalFile delegate) {
        super(delegate.path());
    }

    @Override
    public byte[] hash() {
        return DestinationCacheDatabase.getInstance().getHash(path())
                .orElseGet(() -> {
                    final byte[] hash = super.hash();
                    DestinationCacheDatabase.getInstance().put(path(), hash);
                    return hash;
                });
    }
}
