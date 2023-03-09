package backup.domain.time;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class FixedSystemTimeProvider implements SystemTimeProvider {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");

    public static FixedSystemTimeProvider of(String time) {
        return new FixedSystemTimeProvider(LocalDateTime.parse(time, FORMATTER));
    }

    private final LocalDateTime time;

    public FixedSystemTimeProvider(LocalDateTime time) {
        this.time = Objects.requireNonNull(time);
    }

    @Override
    public LocalDateTime now() {
        return time;
    }
}
