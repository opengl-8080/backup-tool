package backup.domain;

import backup.domain.cache.DestinationCacheDatabase;
import backup.domain.file.LocalDirectory;
import backup.domain.file.LocalFile;
import backup.domain.measure.StopWatch;
import backup.domain.plan.BackupPlan;
import backup.domain.plan.BackupPlanner;
import backup.domain.plan.BackupPlans;
import backup.domain.thread.MultiThreadWorker;
import backup.domain.thread.WorkerContext;

import java.util.Objects;

public class BackupService {
    private final LocalDirectory originDirectory;
    private final LocalDirectory destinationDirectory;

    public BackupService(LocalDirectory originDirectory, LocalDirectory destinationDirectory) {
        this.originDirectory = Objects.requireNonNull(originDirectory);
        this.destinationDirectory = Objects.requireNonNull(destinationDirectory);
    }

    public void backup() {
        StopWatch.measure("backup", () -> {
            DestinationCacheDatabase.getInstance().restoreFromFile();

            final BackupPlanner planner = new BackupPlanner(originDirectory, destinationDirectory);
            final BackupPlans plans = planner.plan();

            doBackup(plans);

            DestinationCacheDatabase.getInstance().saveToFile();
        });
    }

    private void doBackup(BackupPlans plans) {
        StopWatch.measure("doBackup", () -> {
            final WorkerContext<Void> context = MultiThreadWorker.getInstance().newContext();

            for (BackupPlan plan : plans) {
                context.submit(() -> {
                    final LocalFile originFile = originDirectory.resolveFile(plan.path());
                    final LocalFile destinationFile = destinationDirectory.resolveFile(plan.path());

                    plan.operation().execute(originFile, destinationFile);
                });
            }

            context.join();
        });
    }
}
