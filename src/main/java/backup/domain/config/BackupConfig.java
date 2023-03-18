package backup.domain.config;

import backup.domain.file.LocalDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class BackupConfig {

    public static BackupConfig load(Path configFile) {
        final Properties props = new Properties();

        try (final BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8);) {
            props.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new BackupConfig(props);
    }

    private final Properties props;
    private final Path home;

    private BackupConfig(Properties props) {
        this.props = props;
        this.home = Path.of(props.getProperty("home", "."));
    }

    public Path logFile() {
        return home.resolve("backup.log");
    }

    public Path destinationCache() {
        return home.resolve("destination-cache.dat");
    }

    public LocalDirectory originDirectory() {
        if (!props.containsKey("origin")) {
            throw new RuntimeException("origin (バックアップ元ディレクトリ)が設定ファイルに設定されていません");
        }
        return new LocalDirectory(Path.of(props.getProperty("origin")));
    }

    public LocalDirectory destinationDirectory() {
        if (!props.containsKey("destination")) {
            throw new RuntimeException("destination (バックアップ咲ディレクトリ)が設定ファイルに設定されていません");
        }
        return new LocalDirectory(Path.of(props.getProperty("destination")));
    }
}
