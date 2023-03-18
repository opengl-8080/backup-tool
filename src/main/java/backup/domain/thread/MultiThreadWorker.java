package backup.domain.thread;

import java.util.concurrent.ForkJoinPool;

public class MultiThreadWorker {
    private static final MultiThreadWorker INSTANCE = new MultiThreadWorker();

    public static MultiThreadWorker getInstance() {
        return INSTANCE;
    }

    private final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    public <T> WorkerContext<T> newContext() {
        return new WorkerContext<>(pool);
    }

    private MultiThreadWorker() {}
}
