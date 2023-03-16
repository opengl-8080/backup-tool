package backup.domain.measure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StopWatch {
    private static boolean ENABLE = false;
    private static Map<String, AtomicLong> TIMES = new ConcurrentHashMap<>();

    public static void enable() {
        ENABLE = true;
    }

    public static void printTimes() {
        for (Map.Entry<String, AtomicLong> entry : TIMES.entrySet()) {
            final String tag = entry.getKey();
            final AtomicLong time = entry.getValue();
            System.out.printf("%s\t%d%n", tag, time.longValue());
        }
    }

    public static Statistics dumpStatistics() {
        return new Statistics(TIMES);
    }

    public static void reset() {
        TIMES = new ConcurrentHashMap<>();
    }

    private final long begin = System.currentTimeMillis();
    private final String tag;

    public static StopWatch start(String tag) {
        return new StopWatch(tag);
    }

    private StopWatch(String tag) {
        this.tag = tag;
    }

    public void stop() {
        if (!ENABLE) {
            return;
        }
        final long time = System.currentTimeMillis() - begin;
        TIMES.computeIfAbsent(tag, (key) -> new AtomicLong(0L)).getAndAdd(time);
    }
}
