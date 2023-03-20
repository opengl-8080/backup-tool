package backup.domain;

import backup.domain.cache.DestinationCacheDatabase;
import backup.domain.config.BackupContext;
import backup.domain.file.LocalDirectory;
import backup.domain.file.LocalFile;
import backup.domain.logging.Logger;
import backup.domain.logging.Progress;
import backup.domain.measure.StopWatch;
import backup.domain.plan.BackupPlan;
import backup.domain.plan.BackupPlanner;
import backup.domain.plan.BackupPlans;
import backup.domain.thread.MultiThreadWorker;
import backup.domain.thread.WorkerContext;

import java.util.Objects;

public class BackupService {
    private final BackupContext context;
    private final Logger logger;
    private final DestinationCacheDatabase cache;
    private final LocalDirectory originDirectory;
    private final LocalDirectory destinationDirectory;

    public BackupService(BackupContext context) {
        this.context = Objects.requireNonNull(context);
        this.logger = new Logger(context.name(), context.logFile());
        this.cache = new DestinationCacheDatabase(context.destinationCache());
        this.originDirectory = context.originDirectory();
        this.destinationDirectory = context.destinationDirectory();
    }

    public DestinationCacheDatabase getCache() {
        return cache;
    }

    public WorkerContext<Void> backup() {
        return StopWatch.measure("backup", () -> {
            logger.info("Planning... (origin=%s, destination=%s)".formatted(
                originDirectory.path(), destinationDirectory.path()
            ));

            cache.restoreFromFile();
            Runtime.getRuntime().addShutdownHook(new Thread(cache::saveToFile));

            final BackupPlanner planner = new BackupPlanner(cache, originDirectory, destinationDirectory);
            final BackupPlans plans = planner.plan();

            logger.info("Start backup (add=%d, update=%d, remove=%d).".formatted(
                plans.addCount(), plans.updateCount(), plans.removeCount()
            ));

            return doBackup(plans);
        });
    }

    private WorkerContext<Void> doBackup(BackupPlans plans) {

        return StopWatch.measure("doBackup", () -> {
            final Progress progress = new Progress(context.name(), plans.totalCount());
            logger.info(progress.currentProgress());

            final WorkerContext<Void> context = MultiThreadWorker.getInstance().newContext();

            for (BackupPlan plan : plans) {
                context.submit(() -> {
                    final LocalFile originFile = originDirectory.resolveFile(plan.path());
                    final LocalFile destinationFile = destinationDirectory.resolveFile(plan.path());

                    plan.operation().execute(logger, originFile, destinationFile);

                    progress.increment().ifPresent(logger::info);
                });
            }

            return context;
        });
    }
}
