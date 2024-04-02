package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.schema.PropertyDescription;

import java.util.Collection;
import java.util.Map;

public abstract class RelationshipAsOwnedTablePropertyMapping extends RelationshipAsTablePropertyMapping {
    protected final Map<Column, ColumnMapping> foreignKeyMapToOwner;
    protected final Map<Column, ColumnMapping> foreignKeyMapToContent;
    protected Table table;

    public RelationshipAsOwnedTablePropertyMapping(ClassMapping classMapping, PropertyDescription propertyDescription,
                                                   String tableName, Map<Column, ColumnMapping> foreignKeyMapToOwner,
                                                   Map<Column, ColumnMapping> foreignKeyMapToContent) {
        super(classMapping, propertyDescription);
        this.table = classMapping.getSchemaMapping().getSchema().getTable(tableName);
        this.foreignKeyMapToOwner = foreignKeyMapToOwner;
        this.foreignKeyMapToContent = foreignKeyMapToContent;
    }

    public Map<Column, ColumnMapping> getForeignKeyMapToContent() {
        return foreignKeyMapToContent;
    }

    public Map<Column, ColumnMapping> getForeignKeyMapToOwner() {
        return foreignKeyMapToOwner;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    @Override
    public void collectUsedTables(Collection<Table> collection) {
        collection.add(table);
    }

    @Override
    public void collectUsedColumns(Collection<Column> collection) {
        for (Map.Entry<Column, ColumnMapping> entry : foreignKeyMapToOwner.entrySet()) {
            if (!collection.contains(entry.getKey())) {
                collection.add(entry.getKey());
            }
        }
        for (Map.Entry<Column, ColumnMapping> entry : foreignKeyMapToContent.entrySet()) {
            if (!collection.contains(entry.getKey())) {
                collection.add(entry.getKey());
            }
        }
    }


}
