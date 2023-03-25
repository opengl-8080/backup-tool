package backup.domain.shutdown;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownService {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final ShutdownExecutor executor;
    private final long waitMilliseconds;
    private boolean permitted;
    private boolean waitTimeHasBeenPassed;

    public ShutdownService(ShutdownExecutor executor, long waitMilliseconds) {
        this.executor = Objects.requireNonNull(executor);
        this.waitMilliseconds = waitMilliseconds;
    }

    public void start() {
        executorService.schedule(this::challengeShutdown, waitMilliseconds, TimeUnit.MILLISECONDS);
    }

    private synchronized void challengeShutdown() {
        if (permitted) {
            executor.shutdown();
        }
        executorService.shutdown();
        waitTimeHasBeenPassed = true;
    }

    public synchronized void permit() {
        permitted = true;
        if (waitTimeHasBeenPassed) {
            executor.shutdown();
        }
    }
}
