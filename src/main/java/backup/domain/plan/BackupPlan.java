package backup.domain.plan;

import java.nio.file.Path;

public record BackupPlan(
    Operation operation,
    Path path
) {
}
