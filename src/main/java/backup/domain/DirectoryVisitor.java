package backup.domain;

public interface DirectoryVisitor<D extends Directory> {
    VisitResult visit(D directory);
}
