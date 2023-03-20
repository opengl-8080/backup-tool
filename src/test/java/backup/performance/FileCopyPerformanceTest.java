package backup.performance;

import backup.domain.file.LocalFile;
import backup.domain.logging.Logger;
import backup.domain.plan.Operation;
import backup.domain.thread.MultiThreadWorker;
import backup.domain.thread.WorkerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileCopyPerformanceTest {

    @TempDir
    Path tempDir;

    static byte[] content = new byte[1024*1024*128];

    List<Long> times = new CopyOnWriteArrayList<>();
    long start;

    final int loopCount = 100;

    @BeforeAll
    static void beforeAll() {
        Arrays.fill(content, (byte)0);
    }

    @BeforeEach
    void setUp() throws Exception {
        System.out.println("warm up...");
        for (int i=0; i<10; i++) {
            Path originPath = tempDir.resolve("origin");
            Files.write(originPath, content);
            Path destinationPath = tempDir.resolve("destination");

            Operation.ADD.execute(Logger.nullLogger(), new LocalFile(originPath), new LocalFile(destinationPath));

            Files.delete(originPath);
            Files.delete(destinationPath);
        }
    }

    @AfterEach
    void tearDown() {
        final long total = System.currentTimeMillis() - start;

        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        long sum = 0;
        for (Long time : times) {
            max = Math.max(time, max);
            min = Math.min(time, min);
            sum += time;
        }
        System.out.printf("total\t%d\taverage\t%d\tmin\t%d\tmax\t%d%n", total, (sum/times.size()), min, max);
    }

    @ParameterizedTest
//    @ValueSource(ints = {1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22})
    @ValueSource(ints = {10})
    void multiThread(int poolSize) throws Exception {
        System.out.println("create origin files...");
        for (int i=0; i<loopCount; i++) {
            Path originPath = tempDir.resolve("origin" + 1);
            Files.write(originPath, content);
        }

        MultiThreadWorker.getInstance().init(poolSize);
        final WorkerContext<Object> context = MultiThreadWorker.getInstance().newContext();

        start = System.currentTimeMillis();

        System.out.println("measuring (poolSize=" + poolSize + ") ...");
        for (int i=0; i<loopCount; i++) {
            Path originPath = tempDir.resolve("origin" + 1);
            Path destinationPath = tempDir.resolve("destination" + i);

            context.submit(() -> {
                final long begin = System.currentTimeMillis();
                Operation.ADD.execute(Logger.nullLogger(), new LocalFile(originPath), new LocalFile(destinationPath));
                final long time = System.currentTimeMillis() - begin;
                times.add(time);
            });
        }

        context.join();
    }
}
