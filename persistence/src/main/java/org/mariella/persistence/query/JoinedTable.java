package org.mariella.persistence.query;


import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Table;

public class JoinedTable implements TableReference {
    private boolean referenced = false;

    private String alias;
    private Table table;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String identifier) {
        this.alias = identifier;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    @Override
    public boolean isReferenced() {
        return referenced;
    }

    @Override
    public void printSql(StringBuilder b) {
        if (alias != null) {
            b.append(alias);
        } else {
            b.append(table.getQualifiedName());
        }
    }

    @Override
    public void printFromClause(StringBuilder b) {
        b.append(table.getQualifiedName());
        if (alias != null) {
            b.append(" ");
            b.append(alias);
        }
    }

    @Override
    public ColumnReference createColumnReference(Column column) {
        referenced = true;
        return createUnreferencedColumnReference(column);
    }

    @Override
    public ColumnReference createUnreferencedColumnReference(Column column) {
        return new ColumnReferenceImpl(this, column);
    }

    @Override
    public ColumnReference createColumnReferenceForRelationship(Column foreignKeyColumn) {
        return createColumnReference(foreignKeyColumn);
    }

    @Override
    public boolean canCreateColumnReference(Column column) {
        return table.getColumns().contains(column);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        printSql(b);
        return b.toString();
    }
}
