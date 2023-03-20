package backup.domain.thread;

import java.util.concurrent.ForkJoinPool;

public class MultiThreadWorker {
    private static final MultiThreadWorker INSTANCE = new MultiThreadWorker();

    public static MultiThreadWorker getInstance() {
        return INSTANCE;
    }

    private ForkJoinPool pool;

    public synchronized  <T> WorkerContext<T> newContext() {
        if (pool == null) {
            pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        }
        return new WorkerContext<>(pool);
    }

    public void init(int poolSize) {
        pool = new ForkJoinPool(poolSize);
    }

    private MultiThreadWorker() {}
}
