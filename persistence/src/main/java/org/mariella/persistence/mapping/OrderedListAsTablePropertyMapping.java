package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.persistor.Row;
import org.mariella.persistence.query.RelationshipAsTableJoinBuilder;
import org.mariella.persistence.query.SubSelectBuilder;
import org.mariella.persistence.query.TableReference;
import org.mariella.persistence.runtime.CollectionModificationInfo;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.schema.PropertyDescription;
import org.mariella.persistence.util.Util;

import java.util.List;
import java.util.Map;

public class OrderedListAsTablePropertyMapping extends RelationshipAsOwnedTablePropertyMapping {
    private final Column orderColumn;

    public OrderedListAsTablePropertyMapping(ClassMapping classMapping, PropertyDescription propertyDescription, String tableName,
                                             Map<Column, ColumnMapping> foreignKeyMapToOwner,
                                             Map<Column, ColumnMapping> foreignKeyMapToContent, String orderColumnName) {
        super(classMapping, propertyDescription, tableName, foreignKeyMapToOwner, foreignKeyMapToContent);
        this.orderColumn = table.getColumn(orderColumnName);
        Util.assertTrue(orderColumn != null, "Unknown column");
    }

    @Override
    public RelationshipAsTableJoinBuilder createJoinBuilder(SubSelectBuilder subSelectBuilder, TableReference myTableReference) {
        RelationshipAsTableJoinBuilder joinBuilder = super.createJoinBuilder(subSelectBuilder, myTableReference);
        joinBuilder.getJoinTableJoinBuilder().getOrderBy()
                .add(joinBuilder.getJoinTableJoinBuilder().getJoinedTableReference().createColumnReference(orderColumn));
        return joinBuilder;
    }

    @Override
    protected RelationshipAsTableJoinBuilder createReverseJoinBuilder(SubSelectBuilder subSelectBuilder,
                                                                      TableReference contentTableReference) {
        RelationshipAsTableJoinBuilder joinBuilder = super.createReverseJoinBuilder(subSelectBuilder, contentTableReference);
        joinBuilder.getJoinTableJoinBuilder().getOrderBy()
                .add(joinBuilder.getJoinTableJoinBuilder().getJoinedTableReference().createColumnReference(orderColumn));
        return joinBuilder;
    }

    @Override
    protected void persistPrimary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        CollectionModificationInfo cmi = persistor.getModificationInfo()
                .getCollectionModificationInfo(getPropertyDescription().getPropertyDescriptor().getName());
        if (cmi != null && cmi.hasChanges()) {
            Row row = new Row(table);
            for (Map.Entry<Column, ColumnMapping> entry : foreignKeyMapToOwner.entrySet()) {
                Object ownerValue = ModifiableAccessor.Singleton.getValue(persistor.getModificationInfo().getObject(),
                        entry.getValue().getPropertyDescription());
                row.setProperty(entry.getKey(), ownerValue);
            }
            persistor.getSetColumnsDeleteStatement(this).addBatch(row, null);
        }
    }

    @Override
    public void persistSecondary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        CollectionModificationInfo cmi = persistor.getModificationInfo()
                .getCollectionModificationInfo(getPropertyDescription().getPropertyDescriptor().getName());
        if (cmi != null && cmi.hasChanges()) {
            List<?> elements = (List<?>) value;
            for (int i = 0; i < elements.size(); i++) {
                Object element = elements.get(i);
                Row row = new Row(table);
                setRowValues(row, persistor.getModificationInfo().getObject(), element);
                row.setProperty(orderColumn, i);
                persistor.getBatchInsertStatement(this).addBatch(row, null);
            }
        }
    }


}
