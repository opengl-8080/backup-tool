package backup.domain.time;

import java.time.LocalDateTime;

public class DefaultSystemTimeProvider implements SystemTimeProvider {
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
