package util;

public enum VDProperties {
    SourceDatabaseName("SourceDatabaseName"),
    ResultDatabaseName("ResultDatabaseName"),
    NodeDatabaseName("NodeDatabaseName"),
    EdgeDatabaseName("EdgeDatabaseName"),
    SelectedModule("SelectedModule");
    private final String val;
    VDProperties(final String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}
