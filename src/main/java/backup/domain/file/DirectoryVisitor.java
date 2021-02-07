package backup.domain.file;

public interface DirectoryVisitor<D extends Directory> {
    VisitResult visit(D directory);
}
