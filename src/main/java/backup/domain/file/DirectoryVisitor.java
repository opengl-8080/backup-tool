package backup.domain.file;

import java.nio.file.Path;

public interface DirectoryVisitor {
    void visit(LocalFile file, Path relativePath);
}
