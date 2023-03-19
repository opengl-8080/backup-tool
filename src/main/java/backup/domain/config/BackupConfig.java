package backup.domain.config;

import backup.domain.file.LocalDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackupConfig {
    private static final Pattern KEY_PATTERN = Pattern.compile("^(origin|destination)\\.(?<name>.+)$");

    public static BackupConfig load(Path configFile) {
        final Properties props = new Properties();

        try (final BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8);) {
            props.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path home = Path.of(props.getProperty("home", "."));

        Map<String, BackupContext.Builder> builders = new HashMap<>();

        for (Object key : props.keySet()) {
            final String textKey = (String) key;
            final Matcher matcher = KEY_PATTERN.matcher(textKey);

            if (matcher.find()) {
                final String name = matcher.group("name");

                final BackupContext.Builder builder = builders.computeIfAbsent(name, n -> new BackupContext.Builder(home, name));

                if (textKey.startsWith("origin.")) {
                    final String origin = props.getProperty(textKey);
                    builder.setOriginDirectory(new LocalDirectory(Path.of(origin)));
                } else if (textKey.startsWith("destination.")) {
                    final String destination = props.getProperty(textKey);
                    builder.setDestinationDirectory(new LocalDirectory(Path.of(destination)));
                }
            }
        }

        final List<BackupContext> contexts = builders.values().stream().map(BackupContext.Builder::build).toList();

        return new BackupConfig(contexts);
    }

    private final List<BackupContext> contexts;

    private BackupConfig(List<BackupContext> contexts) {
        this.contexts = contexts;
    }

    public List<BackupContext> contexts() {
        return contexts;
    }
}
