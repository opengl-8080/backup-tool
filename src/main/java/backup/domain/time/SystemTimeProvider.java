package backup.domain.time;

import java.time.LocalDateTime;

public interface SystemTimeProvider {
    LocalDateTime now();
}
