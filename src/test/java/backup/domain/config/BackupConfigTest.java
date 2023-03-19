package backup.domain.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BackupConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void test() {
        Path config = config("""
        home=foo/bar
        origin.hoge=hoge/origin
        destination.hoge=hoge/destination
        origin.fuga=fuga/origin
        destination.fuga=fuga/destination
        """);

        final BackupConfig sut = BackupConfig.load(config);

        assertThat(sut.contexts()).satisfiesExactlyInAnyOrder(
            context -> {
                assertThat(context.name()).isEqualTo("hoge");
                assertThat(context.originDirectory().path()).isEqualTo(Path.of("hoge/origin"));
                assertThat(context.destinationDirectory().path()).isEqualTo(Path.of("hoge/destination"));
                assertThat(context.destinationCache()).isEqualTo(Path.of("foo/bar/cache/destination-cache-hoge.dat"));
                assertThat(context.logFile()).isEqualTo(Path.of("foo/bar/log/backup-hoge.log"));
            },
            context -> {
                assertThat(context.name()).isEqualTo("fuga");
                assertThat(context.originDirectory().path()).isEqualTo(Path.of("fuga/origin"));
                assertThat(context.destinationDirectory().path()).isEqualTo(Path.of("fuga/destination"));
                assertThat(context.destinationCache()).isEqualTo(Path.of("foo/bar/cache/destination-cache-fuga.dat"));
                assertThat(context.logFile()).isEqualTo(Path.of("foo/bar/log/backup-fuga.log"));
            }
        );
    }

    private Path config(String content) {
        final Path config = tempDir.resolve("test.properties");
        try {
            Files.writeString(config, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return config;
    }
}