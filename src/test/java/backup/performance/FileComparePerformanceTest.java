package backup.performance;

import backup.domain.file.LocalFile;
import backup.domain.thread.MultiThreadWorker;
import backup.domain.thread.WorkerContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

public class FileComparePerformanceTest {

    static final byte[] content = new byte[1024*1024*512];
    static final int loopCount = 100;
    static final Path ssdDir = Path.of("D:/tmp");
    static final Path hddDir = Path.of("E:/tmp");
    static final Path origin = ssdDir.resolve("origin");
    static final Path destination = hddDir.resolve("destination");
    static final LocalFile originFile = new LocalFile(origin);
    static final LocalFile destinationFile = new LocalFile(destination);
    static final int poolSize = 1;

    WorkerContext<Void> context;

    @BeforeAll
    static void beforeAll() throws Exception {
        Arrays.fill(content, (byte)0);

        System.out.println("init files...");
        Files.write(origin, content);
        Files.write(destination, content);

        for (int i=0; i<loopCount; i++) {
            final Path orig = ssdDir.resolve("origin" + i);
            final Path dest = hddDir.resolve("destination" + i);
            if (!orig.toFile().exists()) {
                Files.copy(origin, orig);
            }
            if (!dest.toFile().exists()) {
                Files.copy(destination, dest);
            }
        }

        System.out.println("warm up...");
        for (int i=0; i<100; i++) {
            originFile.calcHash();
            fileEquals(origin, destination);
        }
    }

    @BeforeEach
    void setUp() {
        MultiThreadWorker.getInstance().init(poolSize);
        context = MultiThreadWorker.getInstance().newContext();
    }

    /**
     * 1	28670	286
     */
//    @Test
    void testHashSingleThread() {
        final byte[] destinationFileHash = destinationFile.calcHash();

        System.out.println("start...");
        long sum = 0;
        for (int i=0; i<loopCount; i++) {
            final long begin = System.currentTimeMillis();

            final byte[] originFileHash = originFile.calcHash();
            assertThat(Arrays.equals(originFileHash, destinationFileHash)).isTrue();

            sum += System.currentTimeMillis() - begin;
        }

        System.out.printf("%d\t%d\t%d", poolSize, sum, (sum/loopCount));
    }

    /**
     * 16: total = 96371, average = 14923
     * 2	96772	1935
     * 3	96498	2874
     */
    @Test
    void testHashMultiThread() {
        final byte[] destinationFileHash = destinationFile.calcHash();

        final long start = System.currentTimeMillis();
        AtomicLong sum = new AtomicLong(0L);

        System.out.println("start...");
        for (int i=0; i<loopCount; i++) {
            final LocalFile file = new LocalFile(ssdDir.resolve("origin" + i));

            context.submit(() -> {
                final long begin = System.currentTimeMillis();

                final byte[] originFileHash = file.calcHash();
                assertThat(Arrays.equals(originFileHash, destinationFileHash)).isTrue();

                final long time = System.currentTimeMillis() - begin;
                sum.addAndGet(time);
            });
        }
        context.join();
        final long total = System.currentTimeMillis() - start;

        System.out.printf("%d\t%d\t%d", poolSize, total, (sum.get()/loopCount));
    }

    static final int bufferSize = 1024*1024;

    /**
     * 1	31951	319
     */
    @Test
    void testAllCompareSingleThread() {

        System.out.println("start...");
        long sum = 0;
        for (int i=0; i<loopCount; i++) {
            final long begin = System.currentTimeMillis();

            assertThat(fileEquals(origin, destination)).isTrue();

            sum += System.currentTimeMillis() - begin;
        }

        System.out.printf("%d\t%d\t%d", poolSize, sum, (sum/loopCount));
    }

    /**
     * 1	515957	5159
     * 2	786401	15727
     */
    @Test
    void testAllCompareMultiThread() {
        final long start = System.currentTimeMillis();
        final AtomicLong sum = new AtomicLong(0L);

        System.out.println("start...");
        for (int i=0; i<loopCount; i++) {
            final Path from = ssdDir.resolve("origin" + i);
            final Path to = hddDir.resolve("destination" + i);

            context.submit(() -> {
                final long begin = System.currentTimeMillis();

                assertThat(fileEquals(from, to)).isTrue();

                sum.addAndGet(System.currentTimeMillis() - begin);
            });
        }

        context.join();
        final long total = System.currentTimeMillis() - start;

        System.out.printf("%d\t%d\t%d", poolSize, total, (sum.get()/loopCount));
    }

    private static boolean fileEquals(Path origin, Path destination) {
        byte[] originBuffer = new byte[bufferSize];
        byte[] destinationBuffer = new byte[bufferSize];
        try (
            final InputStream originIn = Files.newInputStream(origin, StandardOpenOption.READ);
            final InputStream destinationIn = Files.newInputStream(destination, StandardOpenOption.READ);
        ) {
            int originSize, destinationSize;
            while ((originSize = originIn.read(originBuffer)) != -1 && (destinationSize = destinationIn.read(destinationBuffer)) != -1) {
                if (originSize != destinationSize) {
                    return false;
                }

                for (int j=0; j<originSize; j++) {
                    if (originBuffer[j] != destinationBuffer[j]) {
                        return false;
                    }
                }
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
