package backup.domain;

import backup.domain.config.BackupContext;
import backup.domain.file.LocalDirectory;
import backup.domain.time.DefaultSystemTimeProvider;
import backup.domain.time.FixedSystemTimeProvider;
import backup.domain.time.SystemTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static backup.domain.TestConditions.fileCount;
import static org.assertj.core.api.Assertions.assertThat;

class BackupServiceTest {
    @RegisterExtension
    final TestFiles testFiles = new TestFiles();

    @TempDir
    Path tempDir;
    BackupContext context;
    BackupService sut;

    @BeforeEach
    void setUp() {
        context = new BackupContext(
            tempDir.resolve("home"),
            "backup-service-test",
            LocalDirectory.of(testFiles.originDir()),
            LocalDirectory.of(testFiles.destinationDir())
        );

        sut = new BackupService(context);
    }

    @AfterEach
    void tearDown() {
        SystemTime.setProvider(new DefaultSystemTimeProvider());
    }

    @Test
    void コピー先のディレクトリが無い場合エラーにならないこと() throws Exception {
        Files.delete(testFiles.destinationDir());

        testFiles.writeOriginFile("test1.txt", "one");

        sut.backup().join();

        assertThat(testFiles.destinationDir()).has(fileCount(1));
        assertThat(testFiles.destinationFile("test1.txt")).hasContent("one");
    }

    @Test
    void コピー先に何もファイルが無い場合() {
        testFiles.writeOriginFile("test1.txt", "one");
        testFiles.writeOriginFile("foo/test2.txt", "two");
        testFiles.writeOriginFile("fizz/buzz/test3.txt", "three");

        sut.backup().join();

        assertThat(testFiles.destinationDir()).has(fileCount(1));
        assertThat(testFiles.destinationFile("test1.txt")).hasContent("one");

        assertThat(testFiles.destinationDir("foo")).has(fileCount(1));
        assertThat(testFiles.destinationFile("foo/test2.txt")).hasContent("two");

        assertThat(testFiles.destinationDir("fizz/buzz")).has(fileCount(1));
        assertThat(testFiles.destinationFile("fizz/buzz/test3.txt")).hasContent("three");
    }

    @Test
    void コピー先に同じ内容のファイルがある場合() {
        testFiles.writeOriginFile("test1.txt", "one");
        testFiles.writeOriginFile("foo/test2.txt", "two");
        testFiles.writeOriginFile("fizz/buzz/test3.txt", "three");
        testFiles.writeDestinationFile("test1.txt", "one");
        testFiles.writeDestinationFile("foo/test2.txt", "two");
        testFiles.writeDestinationFile("fizz/buzz/test3.txt", "three");

        SystemTime.setProvider(FixedSystemTimeProvider.of("2023-03-09 11:22:33.444"));

        sut.backup().join();

        assertThat(testFiles.destinationDir()).has(fileCount(1));
        assertThat(testFiles.destinationFile("test1.txt")).hasContent("one");

        assertThat(testFiles.destinationDir("foo")).has(fileCount(1));
        assertThat(testFiles.destinationFile("foo/test2.txt")).hasContent("two");

        assertThat(testFiles.destinationDir("fizz/buzz")).has(fileCount(1));
        assertThat(testFiles.destinationFile("fizz/buzz/test3.txt")).hasContent("three");
    }

    @Test
    void コピー先に異なる内容のファイルがある場合() {
        testFiles.writeOriginFile("test1.txt", "one");
        testFiles.writeOriginFile("foo/test2.txt", "two");
        testFiles.writeOriginFile("fizz/buzz/test3.txt", "three");

        testFiles.writeDestinationFile("test1.txt", "ONE");
        testFiles.writeDestinationFile("foo/test2.txt", "TWO");
        testFiles.writeDestinationFile("fizz/buzz/test3.txt", "THREE");

        SystemTime.setProvider(FixedSystemTimeProvider.of("2023-03-09 11:22:33.444"));

        sut.backup().join();

        assertThat(testFiles.destinationDir()).has(fileCount(2));
        assertThat(testFiles.destinationFile("test1.txt")).hasContent("one");
        assertThat(testFiles.destinationFile("test1#20230309-112233444.txt")).hasContent("ONE");

        assertThat(testFiles.destinationDir("foo")).has(fileCount(2));
        assertThat(testFiles.destinationFile("foo/test2.txt")).hasContent("two");
        assertThat(testFiles.destinationFile("foo/test2#20230309-112233444.txt")).hasContent("TWO");

        assertThat(testFiles.destinationDir("fizz/buzz")).has(fileCount(2));
        assertThat(testFiles.destinationFile("fizz/buzz/test3.txt")).hasContent("three");
        assertThat(testFiles.destinationFile("fizz/buzz/test3#20230309-112233444.txt")).hasContent("THREE");
    }

    @Test
    void コピー元が削除された場合() {
        // remove test1.txt
        // remove foo/test2.txt
        testFiles.writeOriginFile("foo/test3.txt", "three");
        // remove bar

        testFiles.writeDestinationFile("test1.txt", "one");
        testFiles.writeDestinationFile("foo/test2.txt", "two");
        testFiles.writeDestinationFile("foo/test3.txt", "three");
        testFiles.writeDestinationFile("bar/test4.txt", "four");
        testFiles.writeDestinationFile("bar/fizz/test5.txt", "five");
        testFiles.writeDestinationFile("bar/buzz/test6.txt", "six");
        testFiles.writeDestinationFile("bar/buzz/test6#20220101-001122333.txt", "SIX");

        SystemTime.setProvider(FixedSystemTimeProvider.of("2023-03-09 11:22:33.444"));

        sut.backup().join();

        assertThat(testFiles.destinationDir()).has(fileCount(1));
        assertThat(testFiles.destinationFile("test1#20230309-112233444.txt")).hasContent("one");

        assertThat(testFiles.destinationDir("foo")).has(fileCount(2));
        assertThat(testFiles.destinationFile("foo/test3.txt")).hasContent("three");
        assertThat(testFiles.destinationFile("foo/test2#20230309-112233444.txt")).hasContent("two");

        assertThat(testFiles.destinationDir("bar")).has(fileCount(1));
        assertThat(testFiles.destinationFile("bar/test4#20230309-112233444.txt")).hasContent("four");

        assertThat(testFiles.destinationDir("bar/fizz")).has(fileCount(1));
        assertThat(testFiles.destinationFile("bar/fizz/test5#20230309-112233444.txt")).hasContent("five");

        assertThat(testFiles.destinationDir("bar/buzz")).has(fileCount(2));
        assertThat(testFiles.destinationFile("bar/buzz/test6#20230309-112233444.txt")).hasContent("six");
        assertThat(testFiles.destinationFile("bar/buzz/test6#20220101-001122333.txt")).hasContent("SIX");
    }

    @Test
    void 何度かバックアップを繰り返した場合のテスト() {
        // first
        testFiles.writeOriginFile("001.txt", "one");
        testFiles.writeOriginFile("foo/002.txt", "two");
        testFiles.writeOriginFile("foo/bar/003.txt", "three");

        sut.backup().join();

        assertThat(testFiles.destinationDir()).has(fileCount(1));
        assertThat(testFiles.destinationFile("001.txt")).hasContent("one");

        assertThat(testFiles.destinationFile("foo")).has(fileCount(1));
        assertThat(testFiles.destinationFile("foo/002.txt")).hasContent("two");

        assertThat(testFiles.destinationFile("foo/bar")).has(fileCount(1));
        assertThat(testFiles.destinationFile("foo/bar/003.txt")).hasContent("three");

        // second
        SystemTime.setProvider(FixedSystemTimeProvider.of("2023-03-09 11:22:33.444"));
        testFiles.writeOriginFile("001.txt", "ONE");
        testFiles.writeOriginFile("foo/bar/003.txt", "THREE");
        testFiles.writeOriginFile("fizz/004.txt", "four");

        sut.backup().join();

        assertThat(testFiles.destinationDir()).has(fileCount(2));
        assertThat(testFiles.destinationFile("001.txt")).hasContent("ONE");
        assertThat(testFiles.destinationFile("001#20230309-112233444.txt")).hasContent("one");

        assertThat(testFiles.destinationFile("foo")).has(fileCount(1));
        assertThat(testFiles.destinationFile("foo/002.txt")).hasContent("two");

        assertThat(testFiles.destinationFile("foo/bar")).has(fileCount(2));
        assertThat(testFiles.destinationFile("foo/bar/003.txt")).hasContent("THREE");
        assertThat(testFiles.destinationFile("foo/bar/003#20230309-112233444.txt")).hasContent("three");

        assertThat(testFiles.destinationFile("fizz")).has(fileCount(1));
        assertThat(testFiles.destinationFile("fizz/004.txt")).hasContent("four");

        // third
        SystemTime.setProvider(FixedSystemTimeProvider.of("2023-03-10 22:33:44.555"));

        testFiles.removeOriginFile("001.txt");
        testFiles.writeOriginFile("foo/bar/003.txt", "Three");
        testFiles.writeOriginFile("foo/002.txt", "TWO");

        sut.backup().join();

        assertThat(testFiles.destinationDir()).has(fileCount(2));
        assertThat(testFiles.destinationFile("001#20230310-223344555.txt")).hasContent("ONE");
        assertThat(testFiles.destinationFile("001#20230309-112233444.txt")).hasContent("one");

        assertThat(testFiles.destinationFile("foo")).has(fileCount(2));
        assertThat(testFiles.destinationFile("foo/002.txt")).hasContent("TWO");
        assertThat(testFiles.destinationFile("foo/002#20230310-223344555.txt")).hasContent("two");

        assertThat(testFiles.destinationFile("foo/bar")).has(fileCount(3));
        assertThat(testFiles.destinationFile("foo/bar/003.txt")).hasContent("Three");
        assertThat(testFiles.destinationFile("foo/bar/003#20230310-223344555.txt")).hasContent("THREE");
        assertThat(testFiles.destinationFile("foo/bar/003#20230309-112233444.txt")).hasContent("three");

        assertThat(testFiles.destinationFile("fizz")).has(fileCount(1));
        assertThat(testFiles.destinationFile("fizz/004.txt")).hasContent("four");

        // fourth
        SystemTime.setProvider(FixedSystemTimeProvider.of("2023-03-11 12:13:14.555"));

        testFiles.writeOriginFile("foo/bar/003.txt", "three");

        sut.backup().join();

        assertThat(testFiles.destinationDir()).has(fileCount(2));
        assertThat(testFiles.destinationFile("001#20230310-223344555.txt")).hasContent("ONE");
        assertThat(testFiles.destinationFile("001#20230309-112233444.txt")).hasContent("one");

        assertThat(testFiles.destinationFile("foo")).has(fileCount(2));
        assertThat(testFiles.destinationFile("foo/002.txt")).hasContent("TWO");
        assertThat(testFiles.destinationFile("foo/002#20230310-223344555.txt")).hasContent("two");

        assertThat(testFiles.destinationFile("foo/bar")).has(fileCount(4));
        assertThat(testFiles.destinationFile("foo/bar/003.txt")).hasContent("three");
        assertThat(testFiles.destinationFile("foo/bar/003#20230311-121314555.txt")).hasContent("Three");
        assertThat(testFiles.destinationFile("foo/bar/003#20230310-223344555.txt")).hasContent("THREE");
        assertThat(testFiles.destinationFile("foo/bar/003#20230309-112233444.txt")).hasContent("three");

        assertThat(testFiles.destinationFile("fizz")).has(fileCount(1));
        assertThat(testFiles.destinationFile("fizz/004.txt")).hasContent("four");
    }
}