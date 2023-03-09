package backup.domain.file;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileTest {

    @Test
    void バックアップファイルの場合はisLatestはfalseを返す() {
        final LocalFile sut = LocalFile.of(Path.of("foo/bar/test#20130102-112233444.txt"));
        assertThat(sut.isLatest()).isFalse();
    }

    @Test
    void バックアップファイルでない場合はisLatestはtrueを返す() {
        final LocalFile sut = LocalFile.of(Path.of("foo/bar/test.txt"));
        assertThat(sut.isLatest()).isTrue();
    }
}