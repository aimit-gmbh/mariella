package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.persistor.Row;
import org.mariella.persistence.runtime.CollectionModificationInfo;
import org.mariella.persistence.schema.PropertyDescription;

import java.util.Map;

public class CollectionAsTablePropertyMapping extends RelationshipAsOwnedTablePropertyMapping {

    public CollectionAsTablePropertyMapping(ClassMapping classMapping, PropertyDescription propertyDescription, String tableName,
                                            Map<Column, ColumnMapping> foreignKeyMapToOwner,
                                            Map<Column, ColumnMapping> foreignKeyMapToContent) {
        super(classMapping, propertyDescription, tableName, foreignKeyMapToOwner, foreignKeyMapToContent);
    }

    @Override
    protected void persistPrimary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        CollectionModificationInfo cmi = persistor.getModificationInfo()
                .getCollectionModificationInfo(getPropertyDescription().getPropertyDescriptor().getName());
        if (cmi != null) {
            for (Object removed : cmi.getRemoved()) {
                Row row = new Row(table);
                setRowValues(row, persistor.getModificationInfo().getObject(), removed);
                persistor.getSetColumnsDeleteStatement(this).addBatch(row);
            }
        }
    }

    @Override
    public void persistSecondary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        CollectionModificationInfo cmi = persistor.getModificationInfo()
                .getCollectionModificationInfo(getPropertyDescription().getPropertyDescriptor().getName());
        if (cmi != null) {
            for (Object added : cmi.getAdded()) {
                Row row = new Row(table);
                setRowValues(row, persistor.getModificationInfo().getObject(), added);
                persistor.getBatchInsertStatement(this).addBatch(row);
            }
        }
    }

}
