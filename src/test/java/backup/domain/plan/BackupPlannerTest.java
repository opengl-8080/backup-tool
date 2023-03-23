package backup.domain.plan;

import backup.domain.TestFiles;
import backup.domain.cache.DestinationCacheDatabase;
import backup.domain.file.LocalDirectory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BackupPlannerTest {

    @RegisterExtension
    final TestFiles testFiles = new TestFiles();

    final Path cacheFile = Path.of("./build/planner-test-cache.dat");
    final DestinationCacheDatabase cache = new DestinationCacheDatabase(cacheFile);

    final BackupPlanner sut = new BackupPlanner(
        cache,
        new LocalDirectory(testFiles.originDir()),
        new LocalDirectory(testFiles.destinationDir())
    );

    @Test
    void 新規ファイルのバックアップを計画できる() {
        testFiles.writeOriginFile("one.txt", "aaa");
        testFiles.writeOriginFile("foo/two.txt", "bbb");
        testFiles.writeOriginFile("foo/bar/three.txt", "ccc");

        BackupPlans plans = sut.plan();

        assertThat(plans).containsExactlyInAnyOrder(
            backupPlan(Operation.ADD, "one.txt"),
            backupPlan(Operation.ADD, "foo/two.txt"),
            backupPlan(Operation.ADD, "foo/bar/three.txt")
        );
    }

    @Test
    void 更新ファイルのバックアップを計画できる() {
        testFiles.writeOriginFile("one.txt", "111");
        testFiles.writeOriginFile("two.txt", "XXX");
        testFiles.writeOriginFile("foo/three.txt", "333");
        testFiles.writeOriginFile("foo/four.txt", "YYY");
        testFiles.writeOriginFile("foo/bar/five.txt", "555");
        testFiles.writeOriginFile("foo/bar/six.txt", "ZZZ");

        testFiles.writeDestinationFile("one.txt", "111");
        testFiles.writeDestinationFile("two.txt", "222");
        testFiles.writeDestinationFile("foo/three.txt", "333");
        testFiles.writeDestinationFile("foo/four.txt", "444");
        testFiles.writeDestinationFile("foo/bar/five.txt", "555");
        testFiles.writeDestinationFile("foo/bar/six.txt", "666");

        BackupPlans plans = sut.plan();

        assertThat(plans).containsExactlyInAnyOrder(
            backupPlan(Operation.UPDATE, "two.txt"),
            backupPlan(Operation.UPDATE, "foo/four.txt"),
            backupPlan(Operation.UPDATE, "foo/bar/six.txt")
        );
    }

    @Test
    void 削除ファイルのバックアップを計画できる() {
        testFiles.writeOriginFile("two.txt", "222");
        testFiles.writeOriginFile("foo/four.txt", "444");
        testFiles.writeOriginFile("foo/bar/six.txt", "666");

        testFiles.writeDestinationFile("one.txt", "111");
        testFiles.writeDestinationFile("two.txt", "222");
        testFiles.writeDestinationFile("foo/three.txt", "333");
        testFiles.writeDestinationFile("foo/four.txt", "444");
        testFiles.writeDestinationFile("foo/bar/five.txt", "555");
        testFiles.writeDestinationFile("foo/bar/six.txt", "666");

        BackupPlans plans = sut.plan();

        assertThat(plans).containsExactlyInAnyOrder(
            backupPlan(Operation.REMOVE, "one.txt"),
            backupPlan(Operation.REMOVE, "foo/three.txt"),
            backupPlan(Operation.REMOVE, "foo/bar/five.txt")
        );
    }

    @Test
    void 新規_更新_削除の全てのケースが存在するパターンの確認() {
        testFiles.writeOriginFile("one.txt", "111");
        testFiles.writeOriginFile("two.txt", "222");
        testFiles.writeOriginFile("foo/three.txt", "333");
        testFiles.writeOriginFile("foo/four.txt", "XXX");
        testFiles.writeOriginFile("foo/bar/five.txt", "555");

        testFiles.writeDestinationFile("one.txt", "111");
        testFiles.writeDestinationFile("foo/three.txt", "333");
        testFiles.writeDestinationFile("foo/four.txt", "444");
        testFiles.writeDestinationFile("foo/bar/five.txt", "555");
        testFiles.writeDestinationFile("foo/bar/six.txt", "666");

        BackupPlans plans = sut.plan();

        assertThat(plans).containsExactlyInAnyOrder(
            backupPlan(Operation.ADD, "two.txt"),
            backupPlan(Operation.UPDATE, "foo/four.txt"),
            backupPlan(Operation.REMOVE, "foo/bar/six.txt")
        );
    }

    @Test
    void 更新日時が変わっていない場合は変更されていないと判定されること() {
        testFiles.writeOriginFile("one.txt", "111", 1);
        testFiles.writeOriginFile("foo/two.txt", "222", 2);

        testFiles.writeDestinationFile("one.txt", "xxx", 1);
        testFiles.writeDestinationFile("foo/two.txt", "yyy", 3);

        BackupPlans plans = sut.plan();

        assertThat(plans).containsExactlyInAnyOrder(
            backupPlan(Operation.UPDATE, "foo/two.txt")
        );
    }

    private BackupPlan backupPlan(Operation operation, String path) {
        return new BackupPlan(operation, Path.of(path));
    }
}