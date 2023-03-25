package backup;

import backup.domain.BackupService;
import backup.domain.config.BackupConfig;
import backup.domain.config.BackupContext;
import backup.domain.shutdown.ShutdownService;
import backup.domain.shutdown.WindowsShutdownExecutor;
import backup.domain.thread.MultiThreadWorker;
import backup.domain.thread.WorkerContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        BackupConfig config = null;
        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                config = BackupConfig.load(Path.of(arg.replaceAll("^--config=", "")));
            } else if (arg.equals("-v") || arg.equals("--version")) {
                System.out.println("1.5.0");
                return;
            }
        }

        if (config == null) {
            throw new RuntimeException("コマンドライン引数で設定ファイルを指定してください > --config=path/to/backup.properties");
        }

        MultiThreadWorker.getInstance().init(config.getPoolSize());

        final long waitMilliseconds = TimeUnit.MINUTES.toMillis(config.getShutdownMinutes().orElse(0));
        final ShutdownService shutdownService = new ShutdownService(new WindowsShutdownExecutor(), waitMilliseconds);
        shutdownService.start();
        try {
            List<WorkerContext<Void>> backupWorkerContexts = new ArrayList<>();

            for (BackupContext context : config.contexts()) {
                final BackupService service = new BackupService(context);
                backupWorkerContexts.add(service.backup());
            }

            backupWorkerContexts.forEach(WorkerContext::join);
        } finally {
            shutdownService.permit();
        }
    }
}
