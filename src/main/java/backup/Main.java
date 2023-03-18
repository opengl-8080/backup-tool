package backup;

import backup.domain.BackupService;
import backup.domain.cache.DestinationCacheDatabase;
import backup.domain.config.BackupConfig;
import backup.domain.logging.Logger;

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

        DestinationCacheDatabase.getInstance().setPersistenceFile(config.destinationCache());
        Logger.getInstance().initialize(config.logFile());

        final BackupService service = new BackupService(config.originDirectory(), config.destinationDirectory());
        service.backup();
    }
}
