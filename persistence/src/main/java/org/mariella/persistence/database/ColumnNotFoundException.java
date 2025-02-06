package org.mariella.persistence.database;

public class ColumnNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
    public ColumnNotFoundException(Table table, String columnName) {
        super("Invalid column name " + table.getName() + "." + columnName);
    }
}
