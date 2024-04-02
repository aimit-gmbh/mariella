package org.mariella.persistence.mapping;

import java.io.PrintStream;
import java.util.Arrays;

public class UniqueConstraintInfo {

    private String[] columnNames;

    public void debugPrint(PrintStream out) {
        out.print(" @UniqueContstraint " + Arrays.toString(getColumnNames()));
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

}
