package backup.domain;

import backup.domain.file.LocalDirectory;
import backup.domain.time.DefaultSystemTimeProvider;
import backup.domain.time.FixedSystemTimeProvider;
import backup.domain.time.SystemTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

class BackupServiceTest {
    @RegisterExtension
    TestFiles testFiles = new TestFiles();

    final BackupService sut = new BackupService(
        LocalDirectory.of(testFiles.originDir()),
        LocalDirectory.of(testFiles.destinationDir())
    );

    @AfterEach
    void tearDown() {
        SystemTime.setProvider(new DefaultSystemTimeProvider());
    }

    @Test
    void コピー先に何もファイルが無い場合() {
        testFiles.newOriginFile("test1.txt", "one");
        testFiles.newOriginFile("foo/test2.txt", "two");
        testFiles.newOriginFile("fizz/buzz/test3.txt", "three");

        sut.backup();

        assertThat(testFiles.destinationFile("test1.txt")).hasContent("one");
        assertThat(testFiles.destinationFile("foo/test2.txt")).hasContent("two");
        assertThat(testFiles.destinationFile("fizz/buzz/test3.txt")).hasContent("three");
    }

    @Test
    void コピー先に同じ内容のファイルがある場合() {
        testFiles.newOriginFile("test1.txt", "one");
        testFiles.newOriginFile("foo/test2.txt", "two");
        testFiles.newOriginFile("fizz/buzz/test3.txt", "three");
        testFiles.newDestinationFile("test1.txt", "one");
        testFiles.newDestinationFile("foo/test2.txt", "two");
        testFiles.newDestinationFile("fizz/buzz/test3.txt", "three");

        SystemTime.setProvider(FixedSystemTimeProvider.of("2023-03-09 11:22:33.444"));

        sut.backup();

        assertThat(testFiles.destinationFile("test1.txt")).hasContent("one");
        assertThat(testFiles.destinationFile("foo/test2.txt")).hasContent("two");
        assertThat(testFiles.destinationFile("fizz/buzz/test3.txt")).hasContent("three");

        assertThat(testFiles.destinationFile("test1#20230309-112233444.txt")).doesNotExist();
        assertThat(testFiles.destinationFile("foo/test2#20230309-112233444.txt")).doesNotExist();
        assertThat(testFiles.destinationFile("fizz/buzz/test3#20230309-112233444.txt")).doesNotExist();
    }

    @Test
    void コピー先に異なる内容のファイルがある場合() {
        testFiles.newOriginFile("test1.txt", "one");
        testFiles.newOriginFile("foo/test2.txt", "two");
        testFiles.newOriginFile("fizz/buzz/test3.txt", "three");

        testFiles.newDestinationFile("test1.txt", "ONE");
        testFiles.newDestinationFile("foo/test2.txt", "TWO");
        testFiles.newDestinationFile("fizz/buzz/test3.txt", "THREE");

        SystemTime.setProvider(FixedSystemTimeProvider.of("2023-03-09 11:22:33.444"));

        sut.backup();

        assertThat(testFiles.destinationFile("test1.txt")).hasContent("one");
        assertThat(testFiles.destinationFile("foo/test2.txt")).hasContent("two");
        assertThat(testFiles.destinationFile("fizz/buzz/test3.txt")).hasContent("three");

        assertThat(testFiles.destinationFile("test1#20230309-112233444.txt")).hasContent("ONE");
        assertThat(testFiles.destinationFile("foo/test2#20230309-112233444.txt")).hasContent("TWO");
        assertThat(testFiles.destinationFile("fizz/buzz/test3#20230309-112233444.txt")).hasContent("THREE");
    }

    @Test
    void コピー元が削除された場合() {
        // remove test1.txt
        // remove foo/test2.txt
        testFiles.newOriginFile("foo/test3.txt", "three");
        // remove bar

        testFiles.newDestinationFile("test1.txt", "one");
        testFiles.newDestinationFile("foo/test2.txt", "two");
        testFiles.newDestinationFile("foo/test3.txt", "three");
        testFiles.newDestinationFile("bar/test4.txt", "four");
        testFiles.newDestinationFile("bar/fizz/test5.txt", "five");
        testFiles.newDestinationFile("bar/buzz/test6.txt", "six");
        testFiles.newDestinationFile("bar/buzz/test6#20220101-001122333.txt", "SIX");

        SystemTime.setProvider(FixedSystemTimeProvider.of("2023-03-09 11:22:33.444"));

        sut.backup();

        assertThat(testFiles.destinationFile("test1.txt")).doesNotExist();
        assertThat(testFiles.destinationFile("test1#20230309-112233444.txt")).hasContent("one");

        assertThat(testFiles.destinationFile("foo/test2.txt")).doesNotExist();
        assertThat(testFiles.destinationFile("foo/test2#20230309-112233444.txt")).hasContent("two");

        assertThat(testFiles.destinationFile("foo/test3#20230309-112233444.txt")).doesNotExist();

        assertThat(testFiles.destinationFile("bar/test4.txt")).doesNotExist();
        assertThat(testFiles.destinationFile("bar/test4#20230309-112233444.txt")).hasContent("four");

        assertThat(testFiles.destinationFile("bar/fizz/test5.txt")).doesNotExist();
        assertThat(testFiles.destinationFile("bar/fizz/test5#20230309-112233444.txt")).hasContent("five");
        assertThat(testFiles.destinationFile("bar/buzz/test6.txt")).doesNotExist();
        assertThat(testFiles.destinationFile("bar/buzz/test6#20230309-112233444.txt")).hasContent("six");
        assertThat(testFiles.destinationFile("bar/buzz/test6#20220101-001122333.txt")).hasContent("SIX");
    }
}