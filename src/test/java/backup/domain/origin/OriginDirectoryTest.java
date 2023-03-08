package backup.domain.origin;

import backup.domain.destination.DestinationDirectory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

class OriginDirectoryTest {

    @RegisterExtension
    TestFiles testFiles = new TestFiles();

    @Test
    void コピー先に何もファイルが無い場合() {
        testFiles.newOriginFile("test1.txt", "one");
        testFiles.newOriginFile("foo/test2.txt", "two");
        testFiles.newOriginFile("fizz/buzz/test3.txt", "three");

        final OriginDirectory origin = new OriginDirectory(testFiles.originDir());
        final DestinationDirectory destination = new DestinationDirectory(testFiles.destinationDir());

        origin.backupTo(destination);

        assertThat(testFiles.destinationFile("test1.txt")).hasContent("one");
        assertThat(testFiles.destinationFile("foo/test2.txt")).hasContent("two");
        assertThat(testFiles.destinationFile("fizz/buzz/test3.txt")).hasContent("three");
    }
}