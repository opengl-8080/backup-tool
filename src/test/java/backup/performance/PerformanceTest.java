package backup.performance;

import backup.domain.BackupService;
import backup.domain.TestFiles;
import backup.domain.config.BackupContext;
import backup.domain.file.LocalDirectory;
import backup.domain.measure.Statistics;
import backup.domain.measure.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

public class PerformanceTest {
    static final int FILE_SIZE = 1024 * 1024 * 5;
    @RegisterExtension
    final TestFiles testFiles = new TestFiles();

    final Path originDir = testFiles.originDir();
    final Path destinationDir = testFiles.destinationDir();
    @TempDir
    Path tempDir;
    BackupContext context;
    BackupService sut;

    @BeforeEach
    void setUp() {
        context = new BackupContext(
            tempDir.resolve("home"),
            "performance-test",
            LocalDirectory.of(testFiles.originDir()),
            LocalDirectory.of(testFiles.destinationDir())
        );
        sut = new BackupService(context);
    }

    @Test
    void test() throws Exception {
        System.out.println("warm up");
        for (int i=0; i<5; i++) {
            System.out.println(i);

            PerformanceTestUtil.createDirectory(originDir, 3, 5, 3, 1024);
            sut.backup();

            sut.getCache().reset();
            Files.delete(context.destinationCache());
            testFiles.reset();
        }

        System.out.println("measuring...");
        StopWatch.enable();
        Statistics firstStatistics = new Statistics();
        Statistics secondStatistics = new Statistics();
        Statistics thirdStatistics = new Statistics();
        for (int i=0; i<50; i++) {
            System.out.println(i);

            PerformanceTestUtil.createDirectory(originDir, 2, 4, 3, FILE_SIZE);

            sut.backup();
            firstStatistics.add(StopWatch.dumpStatistics());
            StopWatch.reset();

            modifyOriginDirectoryFiles();

            sut.backup();
            secondStatistics.add(StopWatch.dumpStatistics());
            StopWatch.reset();

            modifyOriginDirectoryFiles();

            sut.backup();
            thirdStatistics.add(StopWatch.dumpStatistics());
            StopWatch.reset();

            sut.getCache().reset();
            testFiles.reset();
            Files.delete(context.destinationCache());
        }
        firstStatistics.print("first");
        secondStatistics.print("second");
        thirdStatistics.print("third");
    }

    private void modifyOriginDirectoryFiles() throws IOException {
        Files.walkFileTree(originDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                final String fileName = file.getFileName().toString();
                if (fileName.contains("001")) {
                    Files.write(file, new byte[] {1, 2, 3}, StandardOpenOption.APPEND);
                } else if (fileName.contains("002") || fileName.startsWith("newFile")) {
                    Files.delete(file);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                final Path file = dir.resolve("newFile" + System.currentTimeMillis() + ".txt");
                final byte[] content = new byte[FILE_SIZE];
                Arrays.fill(content, (byte)0);
                Files.write(file, content);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
