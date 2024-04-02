package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;


public interface ColumnVisitor {
    void visit(Column column);
}
