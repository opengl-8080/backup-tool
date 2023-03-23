package backup.domain.plan;

import backup.domain.TestFiles;
import backup.domain.file.LocalFile;
import backup.domain.logging.Logger;
import backup.domain.time.DefaultSystemTimeProvider;
import backup.domain.time.FixedSystemTimeProvider;
import backup.domain.time.SystemTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static backup.domain.TestConditions.fileCount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OperationTest {
    @RegisterExtension
    TestFiles testFiles = new TestFiles();

    @BeforeEach
    void setUp() {
        SystemTime.setProvider(FixedSystemTimeProvider.of("2023-03-10 11:22:33.444"));
    }

    @AfterEach
    void tearDown() {
        SystemTime.setProvider(new DefaultSystemTimeProvider());
    }

    @Test
    void 拡張子のあるファイルをローテーションできる() {
        final LocalFile originFile = new LocalFile(testFiles.writeOriginFile("001.txt", "ONE"));
        final LocalFile destinationFile = new LocalFile(testFiles.writeDestinationFile("001.txt", "one"));

        Operation.UPDATE.execute(Logger.nullLogger(), originFile, destinationFile);

        assertThat(testFiles.originDir()).has(fileCount(1));
        assertThat(originFile.path()).hasContent("ONE");

        assertThat(testFiles.destinationDir()).has(fileCount(2));
        assertThat(destinationFile.path()).hasContent("ONE");
        assertThat(testFiles.destinationFile("001#20230310-112233444.txt")).hasContent("one");
    }

    @Test
    void ドットを複数含むファイルもローテーションできる() {
        final LocalFile originFile = new LocalFile(testFiles.writeOriginFile("001.hoge.txt", "ONE"));
        final LocalFile destinationFile = new LocalFile(testFiles.writeDestinationFile("001.hoge.txt", "one"));

        Operation.UPDATE.execute(Logger.nullLogger(), originFile, destinationFile);

        assertThat(testFiles.originDir()).has(fileCount(1));
        assertThat(originFile.path()).hasContent("ONE");

        assertThat(testFiles.destinationDir()).has(fileCount(2));
        assertThat(destinationFile.path()).hasContent("ONE");
        assertThat(testFiles.destinationFile("001.hoge#20230310-112233444.txt")).hasContent("one");
    }

    @Test
    void 拡張子を含まないファイルもローテーションできる() {
        final LocalFile originFile = new LocalFile(testFiles.writeOriginFile("001", "ONE"));
        final LocalFile destinationFile = new LocalFile(testFiles.writeDestinationFile("001", "one"));

        Operation.UPDATE.execute(Logger.nullLogger(), originFile, destinationFile);

        assertThat(testFiles.originDir()).has(fileCount(1));
        assertThat(originFile.path()).hasContent("ONE");

        assertThat(testFiles.destinationDir()).has(fileCount(2));
        assertThat(destinationFile.path()).hasContent("ONE");
        assertThat(testFiles.destinationFile("001#20230310-112233444")).hasContent("one");
    }

    @Test
    void コピーされたファイルの最終更新日時はコピー元と同じであること() {
        final LocalFile originFile = new LocalFile(testFiles.writeOriginFile("001", "one", 10));
        final LocalFile destinationFile = new LocalFile(testFiles.destinationFile("001"));

        Operation.ADD.execute(Logger.nullLogger(), originFile, destinationFile);

        assertThat(testFiles.destinationFile("001").toFile().lastModified()).isEqualTo(10);
    }
}