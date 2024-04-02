package org.mariella.persistence.query;

import org.mariella.persistence.database.Column;

public interface TableReference extends FromClauseElement, Expression {
    String getAlias();

    ColumnReference createColumnReference(Column column);

    ColumnReference createColumnReferenceForRelationship(Column foreignKeyColumn);

    ColumnReference createUnreferencedColumnReference(Column column);

    boolean canCreateColumnReference(Column column);

    boolean isReferenced();
}
