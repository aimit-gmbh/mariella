package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.query.ColumnReference;
import org.mariella.persistence.query.TableReference;

import java.util.ArrayList;
import java.util.List;

public class JoinedClassMappingTableReference implements TableReference {
    private final List<TableReference> tableReferences = new ArrayList<>();

    @Override
    public boolean isReferenced() {
        for (TableReference tableReference : tableReferences) {
            if (tableReference.isReferenced()) {
                return true;
            }
        }
        return false;
    }

    public void addTableReference(TableReference tableReference) {
        tableReferences.add(tableReference);
    }

    @Override
    public ColumnReference createUnreferencedColumnReference(Column column) {
        TableReference tableReference = getTableReference(column);
        if (tableReference == null) {
            throw new IllegalArgumentException();
        } else {
            return tableReference.createUnreferencedColumnReference(column);
        }
    }

    @Override
    public ColumnReference createColumnReference(Column column) {
        TableReference tableReference = getTableReference(column);
        if (tableReference == null) {
            throw new IllegalArgumentException();
        } else {
            return tableReference.createColumnReference(column);
        }
    }

    @Override
    public ColumnReference createColumnReferenceForRelationship(Column foreignKeyColumn) {
        TableReference tableReference = getTableReference(foreignKeyColumn);
        if (tableReference == null) {
            throw new IllegalArgumentException();
        } else {
            return tableReference.createColumnReferenceForRelationship(foreignKeyColumn);
        }
    }

    @Override
    public boolean canCreateColumnReference(Column column) {
        for (TableReference tableReference : tableReferences) {
            if (tableReference.canCreateColumnReference(column)) {
                return true;
            }
        }
        return false;
    }

    private TableReference getTableReference(Column column) {
        for (TableReference tableReference : tableReferences) {
            if (tableReference.canCreateColumnReference(column)) {
                return tableReference;
            }
        }
        return null;
    }

    @Override
    public String getAlias() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printFromClause(StringBuilder b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printSql(StringBuilder b) {
        throw new UnsupportedOperationException();
    }

}
