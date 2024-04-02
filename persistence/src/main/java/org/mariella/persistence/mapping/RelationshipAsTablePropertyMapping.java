package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.persistor.Row;
import org.mariella.persistence.query.*;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.schema.PropertyDescription;
import org.mariella.persistence.schema.RelationshipPropertyDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class RelationshipAsTablePropertyMapping extends RelationshipPropertyMapping {

    public RelationshipAsTablePropertyMapping(ClassMapping classMapping, PropertyDescription propertyDescription) {
        super(classMapping, (RelationshipPropertyDescription) propertyDescription);
    }

    public abstract Map<Column, ColumnMapping> getForeignKeyMapToContent();

    public abstract Map<Column, ColumnMapping> getForeignKeyMapToOwner();

    public abstract Table getTable();

    @Override
    public void visitColumns(ColumnVisitor visitor) {
    }

    @Override
    public RelationshipAsTableJoinBuilder createJoinBuilder(final SubSelectBuilder subSelectBuilder,
                                                            TableReference myTableReference) {
        JoinedTable joinedTable = subSelectBuilder.createJoinedTable(getTable());
        JoinBuilderImpl joinBuilder = new JoinBuilderImpl(subSelectBuilder);
        joinBuilder.setJoinedTableReference(joinedTable);
        joinBuilder.setJoinType(JoinBuilder.JoinType.leftouter);
        for (Map.Entry<Column, ColumnMapping> entry : getForeignKeyMapToOwner().entrySet()) {
            joinBuilder.getConditionBuilder(entry.getValue().getReadColumn()).and(
                    BinaryCondition.eq(
                            joinedTable.createColumnReference(entry.getKey()),
                            myTableReference.createColumnReference(entry.getValue().getReadColumn())));
        }

        return new RelationshipAsTableJoinBuilder(
                joinBuilder,
                joinTableJoinBuilder -> {
                    JoinBuilder contentJoinBuilder = getReferencedClassMapping().createJoinBuilder(subSelectBuilder);
                    for (Map.Entry<Column, ColumnMapping> entry : getForeignKeyMapToContent().entrySet()) {
                        contentJoinBuilder.getConditionBuilder(entry.getValue().getReadColumn()).and(
                                BinaryCondition.eq(
                                        joinTableJoinBuilder.getJoinedTableReference().createColumnReference(entry.getKey()),
                                        contentJoinBuilder.getJoinedTableReference()
                                                .createColumnReference(entry.getValue().getReadColumn())));
                    }
                    return contentJoinBuilder;
                });
    }

    @Override
    protected RelationshipAsTableJoinBuilder createReverseJoinBuilder(final SubSelectBuilder subSelectBuilder,
                                                                      TableReference contentTableReference) {
        JoinedTable joinedTable = subSelectBuilder.createJoinedTable(getTable());
        JoinBuilderImpl joinBuilder = new JoinBuilderImpl(subSelectBuilder);
        joinBuilder.setJoinType(JoinBuilder.JoinType.leftouter);
        joinBuilder.setJoinedTableReference(joinedTable);
        for (Map.Entry<Column, ColumnMapping> entry : getForeignKeyMapToContent().entrySet()) {
            joinBuilder.getConditionBuilder(entry.getKey()).and(
                    BinaryCondition.eq(
                            joinedTable.createColumnReference(entry.getKey()),
                            contentTableReference.createColumnReference(entry.getValue().getReadColumn())));
        }

        return new RelationshipAsTableJoinBuilder(
                joinBuilder,
                joinTableJoinBuilder -> {
                    JoinBuilder ownerJoinBuilder = getClassMapping().createJoinBuilder(subSelectBuilder);
                    for (Map.Entry<Column, ColumnMapping> entry : getForeignKeyMapToOwner().entrySet()) {
                        ownerJoinBuilder.getConditionBuilder(entry.getKey()).and(
                                BinaryCondition.eq(
                                        joinTableJoinBuilder.getJoinedTableReference().createColumnReference(entry.getKey()),
                                        ownerJoinBuilder.getJoinedTableReference()
                                                .createColumnReference(entry.getValue().getReadColumn())));
                    }
                    return ownerJoinBuilder;
                });
    }


    protected void setRowValues(Row row, Object owner, Object related) {
        for (Map.Entry<Column, ColumnMapping> entry : getForeignKeyMapToOwner().entrySet()) {
            Object ownerValue = ModifiableAccessor.Singleton.getValue(owner, entry.getValue().getPropertyDescription());
            row.setProperty(entry.getKey(), ownerValue);
        }
        for (Map.Entry<Column, ColumnMapping> entry : getForeignKeyMapToContent().entrySet()) {
            Object relatedValue = ModifiableAccessor.Singleton.getValue(related, entry.getValue().getPropertyDescription());
            row.setProperty(entry.getKey(), relatedValue);
        }
    }

    public List<Column> getColumnsForInsert() {
        return getColumns();
    }

    public List<Column> getColumnsForDelete() {
        return getColumns();
    }

    private List<Column> getColumns() {
        List<Column> columns = new ArrayList<>();
        for (Map.Entry<Column, ColumnMapping> entry : getForeignKeyMapToOwner().entrySet()) {
            columns.add(entry.getKey());
        }
        for (Map.Entry<Column, ColumnMapping> entry : getForeignKeyMapToContent().entrySet()) {
            columns.add(entry.getKey());
        }
        return columns;
    }
}
