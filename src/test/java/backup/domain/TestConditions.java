package backup.domain;

import org.assertj.core.api.Condition;

import java.io.File;
import java.nio.file.Path;

public class TestConditions {

    public static Condition<Path> fileCount(int expectedFileCount) {
        return new Condition<>(path -> {
            final File[] files = path.toFile().listFiles();
            if (files == null ) {
                return expectedFileCount == 0;
            }

            int actualFileCount = 0;
            for (File file : files) {
                if (file.isFile()) {
                    actualFileCount++;
                }
            }

            return actualFileCount == expectedFileCount;
        }, "file count %d", expectedFileCount);
    }
}
