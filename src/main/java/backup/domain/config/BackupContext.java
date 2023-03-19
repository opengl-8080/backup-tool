package backup.domain.config;

import backup.domain.file.LocalDirectory;

import java.nio.file.Path;
import java.util.Objects;

public class BackupContext {
    private final Path home;
    private final String name;
    private final LocalDirectory originDirectory;
    private final LocalDirectory destinationDirectory;

    public BackupContext(Path home, String name, LocalDirectory originDirectory, LocalDirectory destinationDirectory) {
        this.home = Objects.requireNonNull(home);
        this.name = Objects.requireNonNull(name);
        this.originDirectory = Objects.requireNonNull(originDirectory);
        this.destinationDirectory = Objects.requireNonNull(destinationDirectory);
    }

    public String name() {
        return name;
    }

    public LocalDirectory originDirectory() {
        return originDirectory;
    }

    public LocalDirectory destinationDirectory() {
        return destinationDirectory;
    }

    public Path destinationCache() {
        return home.resolve("cache/destination-cache-" + name + ".dat");
    }

    public Path logFile() {
        return home.resolve("log/backup-" + name + ".log");
    }

    public static class Builder {
        private final Path home;
        private final String name;
        private LocalDirectory originDirectory;
        private LocalDirectory destinationDirectory;

        public Builder(Path home, String name) {
            this.home = home;
            this.name = name;
        }

        public void setOriginDirectory(LocalDirectory originDirectory) {
            this.originDirectory = originDirectory;
        }

        public void setDestinationDirectory(LocalDirectory destinationDirectory) {
            this.destinationDirectory = destinationDirectory;
        }

        public BackupContext build() {
            if (originDirectory == null) {
                throw new RuntimeException(name + " のコピー元ディレクトリ(origin." + name + ")が指定されていません");
            }

            if (destinationDirectory == null) {
                throw new RuntimeException(name + " のコピー先ディレクトリ(destination." + name + ")が指定されていません");
            }

            return new BackupContext(home, name, originDirectory, destinationDirectory);
        }
    }

    @Override
    public String toString() {
        return "BackupContext{" +
                "home=" + home +
                ", name='" + name + '\'' +
                ", originDirectory=" + originDirectory +
                ", destinationDirectory=" + destinationDirectory +
                '}';
    }
}
