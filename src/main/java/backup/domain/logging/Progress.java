package backup.domain.logging;

import backup.domain.time.SystemTime;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Progress {
    private final String name;
    private final long total;

    private long current = 0L;

    public Progress(String name, long total) {
        this.name = name;
        this.total = total;
    }

    public synchronized Optional<String> increment() {
        final long before = current;
        current++;

        final int beforeRegion = region(before);
        final int currentRegion = region(current);

        if (0 < currentRegion && beforeRegion != currentRegion) {
            return Optional.of(currentProgress());
        } else {
            return Optional.empty();
        }
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");

    public String currentProgress() {
        final String time = FORMATTER.format(SystemTime.now());

        if (total == 0) {
            return "0/0 (0.0%)";
        }

        return "%d/%d (%.1f%%)".formatted(current, total, percent(current));
    }

    private int region(long step) {
        int percent = (int)percent(step);
        return percent / 10;
    }

    public double percent(long step) {
        return ((double)step / total) * 100;
    }
}
