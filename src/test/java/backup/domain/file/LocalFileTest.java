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

    @Test
    void 拡張子を含むファイルのベース名を取得できる() {
        final LocalFile sut = LocalFile.of(Path.of("foo/bar/test.txt"));

        assertThat(sut.baseName()).isEqualTo("test");
    }

    @Test
    void ドットを複数持つファイル名からベース名を取得できる() {
        final LocalFile sut = LocalFile.of(Path.of("foo/bar/test.hoge.txt"));

        assertThat(sut.baseName()).isEqualTo("test.hoge");
    }

    @Test
    void 拡張子を含まないファイル名からベース名を取得するとファイル名全体が取得されること() {
        final LocalFile sut = LocalFile.of(Path.of("foo/bar/testFileName"));

        assertThat(sut.baseName()).isEqualTo("testFileName");
    }

    @Test
    void 拡張子を含むファイル名から拡張子だけを取得できる() {
        final LocalFile sut = LocalFile.of(Path.of("foo/bar/test.txt"));

        assertThat(sut.extension()).hasValue(".txt");
    }

    @Test
    void ドットを複数含むファイル名から拡張子だけを取得できる() {
        final LocalFile sut = LocalFile.of(Path.of("foo/bar/test.hoge.txt"));

        assertThat(sut.extension()).hasValue(".txt");
    }

    @Test
    void 拡張子を含まないファイル名から拡張子を取得した場合は空が返されること() {
        final LocalFile sut = LocalFile.of(Path.of("foo/bar/testFileName"));

        assertThat(sut.extension()).isEmpty();
    }
}