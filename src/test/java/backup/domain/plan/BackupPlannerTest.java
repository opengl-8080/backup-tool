package backup.domain.plan;

import backup.domain.TestFiles;
import backup.domain.file.LocalDirectory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BackupPlannerTest {

    @RegisterExtension
    final TestFiles testFiles = new TestFiles();

    final BackupPlanner sut = new BackupPlanner(
        new LocalDirectory(testFiles.originDir()),
        new LocalDirectory(testFiles.destinationDir())
    );

    @Test
    void 新規ファイルのバックアップを計画できる() {
        testFiles.newOriginFile("one.txt", "aaa");
        testFiles.newOriginFile("foo/two.txt", "bbb");
        testFiles.newOriginFile("foo/bar/three.txt", "ccc");

        BackupPlans plans = sut.plan();

        assertThat(plans).containsExactlyInAnyOrder(
            backupPlan(Operation.ADD, "one.txt"),
            backupPlan(Operation.ADD, "foo/two.txt"),
            backupPlan(Operation.ADD, "foo/bar/three.txt")
        );
    }

    @Test
    void 更新ファイルのバックアップを計画できる() {
        testFiles.newOriginFile("one.txt", "111");
        testFiles.newOriginFile("two.txt", "XXX");
        testFiles.newOriginFile("foo/three.txt", "333");
        testFiles.newOriginFile("foo/four.txt", "YYY");
        testFiles.newOriginFile("foo/bar/five.txt", "555");
        testFiles.newOriginFile("foo/bar/six.txt", "ZZZ");

        testFiles.newDestinationFile("one.txt", "111");
        testFiles.newDestinationFile("two.txt", "222");
        testFiles.newDestinationFile("foo/three.txt", "333");
        testFiles.newDestinationFile("foo/four.txt", "444");
        testFiles.newDestinationFile("foo/bar/five.txt", "555");
        testFiles.newDestinationFile("foo/bar/six.txt", "666");

        BackupPlans plans = sut.plan();

        assertThat(plans).containsExactlyInAnyOrder(
            backupPlan(Operation.UPDATE, "two.txt"),
            backupPlan(Operation.UPDATE, "foo/four.txt"),
            backupPlan(Operation.UPDATE, "foo/bar/six.txt")
        );
    }

    @Test
    void 削除ファイルのバックアップを計画できる() {
        testFiles.newOriginFile("two.txt", "222");
        testFiles.newOriginFile("foo/four.txt", "444");
        testFiles.newOriginFile("foo/bar/six.txt", "666");

        testFiles.newDestinationFile("one.txt", "111");
        testFiles.newDestinationFile("two.txt", "222");
        testFiles.newDestinationFile("foo/three.txt", "333");
        testFiles.newDestinationFile("foo/four.txt", "444");
        testFiles.newDestinationFile("foo/bar/five.txt", "555");
        testFiles.newDestinationFile("foo/bar/six.txt", "666");

        BackupPlans plans = sut.plan();

        assertThat(plans).containsExactlyInAnyOrder(
            backupPlan(Operation.REMOVE, "one.txt"),
            backupPlan(Operation.REMOVE, "foo/three.txt"),
            backupPlan(Operation.REMOVE, "foo/bar/five.txt")
        );
    }

    @Test
    void 新規_更新_削除の全てのケースが存在するパターンの確認() {
        testFiles.newOriginFile("one.txt", "111");
        testFiles.newOriginFile("two.txt", "222");
        testFiles.newOriginFile("foo/three.txt", "333");
        testFiles.newOriginFile("foo/four.txt", "XXX");
        testFiles.newOriginFile("foo/bar/five.txt", "555");

        testFiles.newDestinationFile("one.txt", "111");
        testFiles.newDestinationFile("foo/three.txt", "333");
        testFiles.newDestinationFile("foo/four.txt", "444");
        testFiles.newDestinationFile("foo/bar/five.txt", "555");
        testFiles.newDestinationFile("foo/bar/six.txt", "666");

        BackupPlans plans = sut.plan();

        assertThat(plans).containsExactlyInAnyOrder(
            backupPlan(Operation.ADD, "two.txt"),
            backupPlan(Operation.UPDATE, "foo/four.txt"),
            backupPlan(Operation.REMOVE, "foo/bar/six.txt")
        );
    }

    private BackupPlan backupPlan(Operation operation, String path) {
        return new BackupPlan(operation, Path.of(path));
    }
}