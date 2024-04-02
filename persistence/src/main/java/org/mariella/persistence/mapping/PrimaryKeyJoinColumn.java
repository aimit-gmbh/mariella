package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.schema.PropertyDescription;

public class PrimaryKeyJoinColumn {
    private Column joinTableColumn;
    private Column primaryTableColumn;
    private PropertyDescription primaryKeyProperty;

    public Column getJoinTableColumn() {
        return joinTableColumn;
    }

    public void setJoinTableColumn(Column myColumn) {
        this.joinTableColumn = myColumn;
    }

    public Column getPrimaryTableColumn() {
        return primaryTableColumn;
    }

    public void setPrimaryTableColumn(Column referencedColumn) {
        this.primaryTableColumn = referencedColumn;
    }

    public PropertyDescription getPrimaryKeyProperty() {
        return primaryKeyProperty;
    }

    public void setPrimaryKeyProperty(PropertyDescription primaryKeyProperty) {
        this.primaryKeyProperty = primaryKeyProperty;
    }

}
