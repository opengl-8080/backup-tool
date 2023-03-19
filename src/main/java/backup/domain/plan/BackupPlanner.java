package backup.domain.plan;

import backup.domain.cache.DestinationCacheDatabase;
import backup.domain.file.CachedLocalFile;
import backup.domain.file.LocalDirectory;
import backup.domain.file.LocalFile;
import backup.domain.measure.StopWatch;
import backup.domain.thread.MultiThreadWorker;
import backup.domain.thread.WorkerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BackupPlanner {
    private final DestinationCacheDatabase cache;
    private final LocalDirectory originDirectory;
    private final LocalDirectory destinationDirectory;

    public BackupPlanner(DestinationCacheDatabase cache,
                         LocalDirectory originDirectory,
                         LocalDirectory destinationDirectory) {
        this.cache = Objects.requireNonNull(cache);
        this.originDirectory = Objects.requireNonNull(originDirectory);
        this.destinationDirectory = Objects.requireNonNull(destinationDirectory);
    }

    public BackupPlans plan() {
        return StopWatch.measure("plan", () -> {

            final WorkerContext<List<BackupPlan>> context = MultiThreadWorker.getInstance().newContext();

            context.submit(this::analyseUpdatedFiles);
            context.submit(this::analyseRemovedFiles);

            List<BackupPlan> plans = new ArrayList<>();

            context.getResultOnlyNonNull().forEach(plans::addAll);

            return new BackupPlans(plans);
        });
    }

    private List<BackupPlan> analyseUpdatedFiles() {
        return StopWatch.measure("analyseUpdatedFiles", () -> {
            final WorkerContext<BackupPlan> context = MultiThreadWorker.getInstance().newContext();

            originDirectory.walk((originFile, relativePath) -> {
                context.submit(() -> {
                    final LocalFile destinationFile = destinationDirectory.resolveFile(relativePath);

                    if (!destinationFile.exists()) {
                        return new BackupPlan(Operation.ADD, relativePath);
                    } else if (!originFile.contentEquals(new CachedLocalFile(cache, destinationFile))) {
                        return new BackupPlan(Operation.UPDATE, relativePath);
                    } else {
                        return null;
                    }
                });
            });

            return context.getResultOnlyNonNull();
        });
    }

    private List<BackupPlan> analyseRemovedFiles() {
        return StopWatch.measure("analyseRemovedFiles", () -> {
            final WorkerContext<BackupPlan> context = MultiThreadWorker.getInstance().newContext();

            destinationDirectory.walk((destinationFile, relativePath) -> {
                context.submit(() -> {
                    if (!destinationFile.isLatest()) {
                        return null;
                    }

                    final LocalFile originFile = originDirectory.resolveFile(relativePath);

                    if (!originFile.exists()) {
                        return new BackupPlan(Operation.REMOVE, relativePath);
                    }

                    return null;
                });
            });

            return context.getResultOnlyNonNull();
        });
    }
}
