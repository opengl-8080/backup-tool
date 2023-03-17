package backup.domain.plan;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BackupPlansTest {
    @Test
    void オペレーションごとの件数を取得できる() {
        BackupPlans sut = new BackupPlans(List.of(
            new BackupPlan(Operation.ADD, Path.of("1.txt")),
            new BackupPlan(Operation.UPDATE, Path.of("2.txt")),
            new BackupPlan(Operation.UPDATE, Path.of("3.txt")),
            new BackupPlan(Operation.REMOVE, Path.of("4.txt")),
            new BackupPlan(Operation.REMOVE, Path.of("5.txt")),
            new BackupPlan(Operation.REMOVE, Path.of("6.txt"))
        ));

        assertThat(sut.addCount()).describedAs("addCount").isEqualTo(1);
        assertThat(sut.updateCount()).describedAs("updateCount").isEqualTo(2);
        assertThat(sut.removeCount()).describedAs("removeCount").isEqualTo(3);
    }
}