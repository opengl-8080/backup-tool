package backup.domain.cache;

import java.nio.file.Path;
import java.util.Objects;

public record DestinationFileCache(
        Path path,
        byte[] hash
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DestinationFileCache that = (DestinationFileCache) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
