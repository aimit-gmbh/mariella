package org.mariella.persistence.query;

import org.mariella.persistence.database.Column;

public interface ColumnReference extends ScalarExpression {
    Column column();
}
