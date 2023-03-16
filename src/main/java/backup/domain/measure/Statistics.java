package backup.domain.measure;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Statistics {
    private int count;
    private final Map<String, Long> times = new HashMap<>();

    public Statistics() {}

    public Statistics(Map<String, ? extends Number> times) {
        for (String tag : times.keySet()) {
            this.times.put(tag, times.get(tag).longValue());
        }
    }

    public void add(Statistics other) {
        for (String tag : other.times.keySet()) {
            final Long sum = times.getOrDefault(tag, 0L);
            times.put(tag, sum + other.times.get(tag));
        }
        count++;
    }

    public void print() {
        for (Map.Entry<String, Long> entry : times.entrySet()) {
            final String tag = entry.getKey();
            final long sum = entry.getValue();
            System.out.printf("%s\t%.2f\n", tag, ((double)sum / count));
        }
    }
}
