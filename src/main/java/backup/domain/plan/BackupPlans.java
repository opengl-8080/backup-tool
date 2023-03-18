package backup.domain.plan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BackupPlans implements Iterable<BackupPlan> {
    private final List<BackupPlan> plans;

    public BackupPlans(List<BackupPlan> plans) {
        this.plans = new ArrayList<>(plans);
    }

    @Override
    public Iterator<BackupPlan> iterator() {
        return plans.iterator();
    }

    public int addCount() {
        return count(Operation.ADD);
    }

    public int updateCount() {
        return count(Operation.UPDATE);
    }

    public int removeCount() {
        return count(Operation.REMOVE);
    }

    public int totalCount() {
        return plans.size();
    }

    private int count(Operation operation) {
        return ((int) plans.stream().filter(plan -> plan.operation() == operation).count());
    }
}
