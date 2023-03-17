package backup.domain;

import backup.domain.file.LocalDirectory;
import backup.domain.file.LocalFile;
import backup.domain.measure.StopWatch;
import backup.domain.plan.BackupPlan;
import backup.domain.plan.BackupPlanner;
import backup.domain.plan.BackupPlans;

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
            final BackupPlanner planner = new BackupPlanner(originDirectory, destinationDirectory);
            final BackupPlans plans = planner.plan();

            for (BackupPlan plan : plans) {
                final LocalFile originFile = originDirectory.resolveFile(plan.path());
                final LocalFile destinationFile = destinationDirectory.resolveFile(plan.path());

                plan.operation().execute(originFile, destinationFile);
            }
        });
    }
}
