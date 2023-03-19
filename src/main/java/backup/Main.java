package backup;

import backup.domain.BackupService;
import backup.domain.config.BackupConfig;
import backup.domain.config.BackupContext;
import backup.domain.thread.MultiThreadWorker;
import backup.domain.thread.WorkerContext;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        BackupConfig config = null;
        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                config = BackupConfig.load(Path.of(arg.replaceAll("^--config=", "")));
            }
        }

        if (config == null) {
            throw new RuntimeException("コマンドライン引数で設定ファイルを指定してください > --config=path/to/backup.properties");
        }

        final WorkerContext<Object> multiThreadContext = MultiThreadWorker.getInstance().newContext();

        for (BackupContext context : config.contexts()) {
            multiThreadContext.submit(() -> {
                final BackupService service = new BackupService(context);
                service.backup();
            });
        }

        multiThreadContext.join();
    }
}
