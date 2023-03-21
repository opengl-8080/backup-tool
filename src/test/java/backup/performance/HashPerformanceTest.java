package backup.performance;

import backup.domain.file.LocalFile;
import backup.domain.thread.MultiThreadWorker;
import backup.domain.thread.WorkerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HashPerformanceTest {

    @TempDir
    static Path tempDir;

    static Path file;
    static LocalFile localFile;
    List<Long> times = new CopyOnWriteArrayList<>();
    long start;

    final int loopCount = 100;

    @BeforeAll
    static void beforeAll() throws Exception {
        file = tempDir.resolve("file");
        byte[] buffer = new byte[1024*1024*512];
        Arrays.fill(buffer, (byte)0);
        Files.write(file, buffer);
    }

    @BeforeEach
    void setUp() {
        localFile = new LocalFile(file);

        System.out.println("warm up...");
        for (int i=0; i<10; i++) {
            localFile.calcHash();
        }

        start = System.currentTimeMillis();
    }

    @AfterEach
    void tearDown() {
        final long end = System.currentTimeMillis() - start;
        System.out.println("total time = " + end);

        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        long sum = 0;
        for (Long time : times) {
            max = Math.max(time, max);
            min = Math.min(time, min);
            sum += time;
        }
        System.out.printf("average = %d, min= %d, max = %d%n", (sum/times.size()), min, max);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22})
    void multiThread(int poolSize) {
        MultiThreadWorker.getInstance().init(poolSize);
        final WorkerContext<Object> context = MultiThreadWorker.getInstance().newContext();

        System.out.println("measuring (poolSize=" + poolSize + ") ...");
        for (int i=0; i<loopCount; i++) {
            context.submit(() -> {
                final long begin = System.currentTimeMillis();
                localFile.calcHash();
                final long time = System.currentTimeMillis() - begin;
                times.add(time);
            });
        }

        context.join();
    }

//    @Test
    void singleThread() {
        System.out.println("measuring...");
        for (int i=0; i<loopCount; i++) {
            final long begin = System.currentTimeMillis();
            localFile.calcHash();
            final long time = System.currentTimeMillis() - begin;
            times.add(time);
        }
    }
}
