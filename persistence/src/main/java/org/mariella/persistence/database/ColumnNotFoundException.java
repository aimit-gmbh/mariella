package org.mariella.persistence.database;

public class ColumnNotFoundException extends RuntimeException {
    public ColumnNotFoundException(Table table, String columnName) {
        super("Invalid column name " + table.getName() + "." + columnName);
    }
}
