package backup;

import backup.domain.BackupService;
import backup.domain.config.BackupConfig;
import backup.domain.config.BackupContext;
import backup.domain.thread.MultiThreadWorker;
import backup.domain.thread.WorkerContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        BackupConfig config = null;
        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                config = BackupConfig.load(Path.of(arg.replaceAll("^--config=", "")));
            } else if (arg.equals("-v") || arg.equals("--version")) {
                System.out.println("1.2.2");
                return;
            }
        }

        if (config == null) {
            throw new RuntimeException("コマンドライン引数で設定ファイルを指定してください > --config=path/to/backup.properties");
        }

        MultiThreadWorker.getInstance().init(config.getPoolSize());

        List<WorkerContext<Void>> backupWorkerContexts = new ArrayList<>();

        for (BackupContext context : config.contexts()) {
            final BackupService service = new BackupService(context);
            backupWorkerContexts.add(service.backup());
        }

        backupWorkerContexts.forEach(WorkerContext::join);
    }
}
