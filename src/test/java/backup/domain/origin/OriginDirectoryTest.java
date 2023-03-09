package backup.domain.origin;

import backup.domain.destination.DestinationDirectory;
import backup.domain.time.DefaultSystemTimeProvider;
import backup.domain.time.FixedSystemTimeProvider;
import backup.domain.time.SystemTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

class OriginDirectoryTest {

    @RegisterExtension
    TestFiles testFiles = new TestFiles();

    final OriginDirectory origin = new OriginDirectory(testFiles.originDir());
    final DestinationDirectory destination = new DestinationDirectory(testFiles.destinationDir());

    @AfterEach
    void tearDown() {
        SystemTime.setProvider(new DefaultSystemTimeProvider());
    }

    @Test
    void コピー先に何もファイルが無い場合() {
        testFiles.newOriginFile("test1.txt", "one");
        testFiles.newOriginFile("foo/test2.txt", "two");
        testFiles.newOriginFile("fizz/buzz/test3.txt", "three");

        origin.backupTo(destination);

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

        origin.backupTo(destination);

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

        origin.backupTo(destination);

        assertThat(testFiles.destinationFile("test1.txt")).hasContent("one");
        assertThat(testFiles.destinationFile("foo/test2.txt")).hasContent("two");
        assertThat(testFiles.destinationFile("fizz/buzz/test3.txt")).hasContent("three");

        assertThat(testFiles.destinationFile("test1#20230309-112233444.txt")).hasContent("ONE");
        assertThat(testFiles.destinationFile("foo/test2#20230309-112233444.txt")).hasContent("TWO");
        assertThat(testFiles.destinationFile("fizz/buzz/test3#20230309-112233444.txt")).hasContent("THREE");
    }
}