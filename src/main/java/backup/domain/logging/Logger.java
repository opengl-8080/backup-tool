package backup.domain.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final Logger INSTANCE = new Logger();

    public static Logger getInstance() {
        return INSTANCE;
    }

    private Writer fileWriter;
    private boolean debugEnable;

    public void initialize(Path logFile) {
        try {
            fileWriter = new BufferedWriter(new OutputStreamWriter(
                    Files.newOutputStream(logFile, StandardOpenOption.APPEND, StandardOpenOption.CREATE),
                    StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }));
    }

    public void setDebugEnable(boolean debugEnable) {
        this.debugEnable = debugEnable;
    }

    public void debug(String message) {
        debug(message, null);
    }

    public void debug(String message, Throwable exception) {
        if (debugEnable) {
            log("DEBUG", message, exception);
        }
    }

    public void infoFileOnly(String message) {
        final String formatted = format("INFO", message, null);
        writeToFile(formatted);
    }

    public void info(String message) {
        info(message, null);
    }

    public void info(String message, Throwable exception) {
        log("INFO", message, exception);
    }

    public void warn(String message) {
        warn(message, null);
    }

    public void warn(String message, Throwable exception) {
        log("WARN", message, exception);
    }

    public void error(String message) {
        error(message, null);
    }

    public void error(String message, Throwable exception) {
        log("ERROR", message, exception);
    }

    private void log(String logLevel, String message, Throwable exception) {
        final String formatted = format(logLevel, message, exception);
        System.out.println(formatted);
        writeToFile(formatted);
    }

    private void writeToFile(String formatted) {
        if (fileWriter == null) {
            return;
        }

        synchronized (this) {
            try {
                fileWriter.write(formatted + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");

    private String format(String logLevel, String message, Throwable exception) {
        String stackTrace = "";
        if (exception != null) {
            final StringWriter buffer = new StringWriter();
            final PrintWriter writer = new PrintWriter(buffer);
            exception.printStackTrace(writer);
            stackTrace = " - " + exception.getMessage() + "\n" + buffer.toString();
        }

        return "%s [%-5s] %s%s".formatted(DATE_TIME_FORMATTER.format(LocalDateTime.now()), logLevel, message, stackTrace);
    }

    private Logger() {}
}
