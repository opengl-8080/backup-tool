package backup.performance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

public class PerformanceTestUtil {
    public static void createDirectory(Path dir, int dirsPerDir, int depth, int filesPerDir, int fileSize) {
        createFile(dir, filesPerDir, fileSize);
        if (depth <= 0) {
            return;
        }

        for (int i=0; i<dirsPerDir; i++) {
            final Path subDir = dir.resolve("dir%03d".formatted(i+1));
            try {
                Files.createDirectories(subDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            createDirectory(subDir, dirsPerDir, depth-1, filesPerDir, fileSize);
        }
    }

    private static void createFile(Path dir, int filesPerDir, int fileSize) {
        for (int j=0; j<filesPerDir; j++) {
            final Path file = dir.resolve("file%03d.txt".formatted(j+1));
            final byte[] content = new byte[fileSize];
            Arrays.fill(content, (byte)0);
            try {
                Files.write(file, content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String tree(Path dir) {
        final File[] files = dir.toFile().listFiles();
        if (files == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (File file : files) {
            sb.append(printFilesRecursively(file, 0));
        }
        return sb.toString();
    }

    private static String printFilesRecursively(File currentFile, int depth) {
        StringBuilder sb = new StringBuilder();
        // ディレクトリの場合、再帰的に処理を行う
        if (currentFile.isDirectory()) {
            sb.append("%s+ %s\n".formatted(getDepthString(depth), currentFile.getName()));
            final File[] files = currentFile.listFiles();
            if (files != null) {
                Stream.of(files)
                        .sorted(Comparator.comparing(File::getName))
                        .forEach(file -> sb.append(printFilesRecursively(file, depth + 1)));
            }
        }
        // ファイルの場合、ファイル名を出力する
        else {
            sb.append("%s- %s (%dbyte)\n".formatted(getDepthString(depth), currentFile.getName(), currentFile.length()));
        }

        return sb.toString();
    }

    // ツリーの深さに応じたスペースを返す
    private static String getDepthString(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }
}
