package backup.domain.time;

import java.time.LocalDateTime;
import java.util.Objects;

public class SystemTime {
    private static SystemTimeProvider provider = new DefaultSystemTimeProvider();

    public static void setProvider(SystemTimeProvider provider) {
        SystemTime.provider = Objects.requireNonNull(provider);
    }

    public static LocalDateTime now() {
        return provider.now();
    }
}
