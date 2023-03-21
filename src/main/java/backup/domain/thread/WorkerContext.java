package backup.domain.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class WorkerContext<T> {
    private final ForkJoinPool pool;
    private final List<ForkJoinTask<T>> tasks = new ArrayList<>();
    private final List<Runnable> finishHooks = new ArrayList<>();

    WorkerContext(ForkJoinPool pool) {
        this.pool = Objects.requireNonNull(pool);
    }

    public void submit(Callable<T> callable) {
        final ForkJoinTask<T> task = pool.submit(callable);
        tasks.add(task);
    }

    public void submit(Runnable runnable) {
        final ForkJoinTask<T> task = pool.submit(() -> {
            runnable.run();
            return null;
        });
        tasks.add(task);
    }

    public List<T> getResultOnlyNonNull() {
        return tasks.stream()
                .map(ForkJoinTask::join)
                .filter(Objects::nonNull)
                .toList();
    }

    public void addFinishedHook(Runnable hook) {
        finishHooks.add(hook);
    }

    public void join() {
        tasks.forEach(ForkJoinTask::join);
        finishHooks.forEach(Runnable::run);
    }
}
