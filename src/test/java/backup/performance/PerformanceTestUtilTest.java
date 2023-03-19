package backup.performance;

import backup.domain.TestFiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class PerformanceTestUtilTest {
    @RegisterExtension
    final TestFiles testFiles = new TestFiles();

    final Path originDir = testFiles.originDir();

    @Test
    void testCreateOrigin_depth1() {
        PerformanceTestUtil.createDirectory(originDir, 5, 1, 3, 1024);

        final String result = PerformanceTestUtil.tree(originDir);

        assertThat(result).isEqualTo("""
        + dir001
          - file001.txt (1024byte)
          - file002.txt (1024byte)
          - file003.txt (1024byte)
        + dir002
          - file001.txt (1024byte)
          - file002.txt (1024byte)
          - file003.txt (1024byte)
        + dir003
          - file001.txt (1024byte)
          - file002.txt (1024byte)
          - file003.txt (1024byte)
        + dir004
          - file001.txt (1024byte)
          - file002.txt (1024byte)
          - file003.txt (1024byte)
        + dir005
          - file001.txt (1024byte)
          - file002.txt (1024byte)
          - file003.txt (1024byte)
        - file001.txt (1024byte)
        - file002.txt (1024byte)
        - file003.txt (1024byte)
        """);
    }

    @Test
    void testCreateOrigin_depth3() {
        PerformanceTestUtil.createDirectory(originDir, 2, 3, 1, 512);

        final String result = PerformanceTestUtil.tree(originDir);

        assertThat(result).isEqualTo("""
        + dir001
          + dir001
            + dir001
              - file001.txt (512byte)
            + dir002
              - file001.txt (512byte)
            - file001.txt (512byte)
          + dir002
            + dir001
              - file001.txt (512byte)
            + dir002
              - file001.txt (512byte)
            - file001.txt (512byte)
          - file001.txt (512byte)
        + dir002
          + dir001
            + dir001
              - file001.txt (512byte)
            + dir002
              - file001.txt (512byte)
            - file001.txt (512byte)
          + dir002
            + dir001
              - file001.txt (512byte)
            + dir002
              - file001.txt (512byte)
            - file001.txt (512byte)
          - file001.txt (512byte)
        - file001.txt (512byte)
        """);
    }

    @Test
    void testTree() {
        testFiles.writeOriginFile("001.txt", "abc");
        testFiles.writeOriginFile("002.txt", "abcd");
        testFiles.writeOriginFile("a/001.txt", "AB");
        testFiles.writeOriginFile("b/001.txt", "ABC");
        testFiles.writeOriginFile("001/001.txt", "abcde");

        String actual = PerformanceTestUtil.tree(testFiles.originDir());

        assertThat(actual).isEqualTo("""
        + 001
          - 001.txt (5byte)
        - 001.txt (3byte)
        - 002.txt (4byte)
        + a
          - 001.txt (2byte)
        + b
          - 001.txt (3byte)
        """);
    }
}
