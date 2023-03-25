package backup.domain.shutdown;

import java.io.IOException;

public class WindowsShutdownExecutor implements ShutdownExecutor {

    @Override
    public void shutdown() {
        final Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("shutdown /s");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
