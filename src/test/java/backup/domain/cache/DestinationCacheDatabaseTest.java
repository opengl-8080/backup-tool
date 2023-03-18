package backup.domain.cache;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DestinationCacheDatabaseTest {
    final Path foo = Path.of("foo/bar");
    final Path fizz = Path.of("fizz/buzz");
    final Path hoge = Path.of("hoge/fuga");
    final Path persistenceFile = Path.of("./build/destination-cache-database.dat");
    final DestinationCacheDatabase sut = DestinationCacheDatabase.getInstance();

    @Test
    void キャッシュの情報をファイルに出力しリストアもできること() {
        sut.setPersistenceFile(persistenceFile);

        sut.put(foo, new byte[] {1, 2, 3, 4});
        sut.put(fizz, new byte[] {5, 6, 7, 8});
        sut.put(hoge, new byte[] {9, 0});

        sut.saveToFile();

        sut.reset();

        assertThat(sut.getHash(foo)).isEmpty();
        assertThat(sut.getHash(fizz)).isEmpty();
        assertThat(sut.getHash(hoge)).isEmpty();

        sut.restoreFromFile();

        assertThat(sut.getHash(foo)).hasValue(new byte[] {1, 2, 3, 4});
        assertThat(sut.getHash(fizz)).hasValue(new byte[] {5, 6, 7, 8});
        assertThat(sut.getHash(hoge)).hasValue(new byte[] {9, 0});
    }
}