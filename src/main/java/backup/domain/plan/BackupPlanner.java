package backup.domain.plan;

import backup.domain.file.LocalDirectory;
import backup.domain.file.LocalFile;
import backup.domain.measure.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BackupPlanner {
    private final LocalDirectory originDirectory;
    private final LocalDirectory destinationDirectory;

    public BackupPlanner(LocalDirectory originDirectory, LocalDirectory destinationDirectory) {
        this.originDirectory = Objects.requireNonNull(originDirectory);
        this.destinationDirectory = Objects.requireNonNull(destinationDirectory);
    }

    public BackupPlans plan() {
        final StopWatch stopWatch = StopWatch.start("plan");
        try {
            List<BackupPlan> plans = new ArrayList<>();

            plans.addAll(analyseUpdatedFiles());
            plans.addAll(analyseRemovedFiles());

            return new BackupPlans(plans);
        } finally {
            stopWatch.stop();
        }
    }

    private List<BackupPlan> analyseUpdatedFiles() {
        final StopWatch stopWatch = StopWatch.start("analyseUpdatedFiles");
        try {
            List<BackupPlan> plans = new ArrayList<>();

            originDirectory.walk((originFile, relativePath) -> {
                final LocalFile destinationFile = destinationDirectory.resolveFile(relativePath);

                if (!destinationFile.exists()) {
                    plans.add(new BackupPlan(Operation.ADD, relativePath));
                } else if (!originFile.contentEquals(destinationFile)) {
                    plans.add(new BackupPlan(Operation.UPDATE, relativePath));
                }
            });

            return plans;
        } finally {
            stopWatch.stop();
        }
    }

    private List<BackupPlan> analyseRemovedFiles() {
        final StopWatch stopWatch = StopWatch.start("analyseRemovedFiles");
        try {
            List<BackupPlan> plans = new ArrayList<>();

            destinationDirectory.walk((destinationFile, relativePath) -> {
                final LocalFile originFile = originDirectory.resolveFile(relativePath);

                if (!originFile.exists()) {
                    plans.add(new BackupPlan(Operation.REMOVE, relativePath));
                }
            });

            return plans;
        } finally {
            stopWatch.stop();
        }
    }
}
