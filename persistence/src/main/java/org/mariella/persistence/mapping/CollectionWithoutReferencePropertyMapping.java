package org.mariella.persistence.mapping;

import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.persistor.Row;
import org.mariella.persistence.query.BinaryCondition;
import org.mariella.persistence.query.JoinBuilder;
import org.mariella.persistence.query.JoinBuilder.JoinType;
import org.mariella.persistence.query.SubSelectBuilder;
import org.mariella.persistence.query.TableReference;
import org.mariella.persistence.runtime.CollectionModificationInfo;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.schema.RelationshipPropertyDescription;

import java.util.List;

public class CollectionWithoutReferencePropertyMapping extends RelationshipPropertyMapping {
    private List<JoinColumn> joinColumns = null;

    public CollectionWithoutReferencePropertyMapping(ClassMapping classMapping,
                                                     RelationshipPropertyDescription propertyDescription) {
        super(classMapping, propertyDescription);
    }

    public List<JoinColumn> getJoinColumns() {
        return joinColumns;
    }

    public void setJoinColumns(List<JoinColumn> joinColumns) {
        this.joinColumns = joinColumns;
    }

    @Override
    public JoinBuilder createJoinBuilder(SubSelectBuilder subSelectBuilder, TableReference myTableReference) {
        if (joinColumns == null) {
            throw new IllegalStateException();
        } else {
            JoinBuilder joinBuilder = getReferencedClassMapping().createJoinBuilder(subSelectBuilder);
            for (JoinColumn joinColumn : joinColumns) {
                joinBuilder.getConditionBuilder(joinColumn.getReferencedReadColumn()).and(
                        BinaryCondition.eq(
                                myTableReference.createColumnReferenceForRelationship(joinColumn.getMyReadColumn()),
                                joinBuilder.getJoinedTableReference()
                                        .createColumnReference(joinColumn.getReferencedReadColumn())));
                joinBuilder.setJoinType(JoinType.leftouter);
            }
            return joinBuilder;
        }
    }

    @Override
    protected JoinBuilder createReverseJoinBuilder(SubSelectBuilder subSelectBuilder, TableReference referencedTableReference) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void persistPrimary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
    }

    @Override
    public void persistSecondary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        CollectionModificationInfo cmi = persistor.getModificationInfo()
                .getCollectionModificationInfo(getPropertyDescription().getPropertyDescriptor().getName());
        if (cmi != null) {
            for (Object removed : cmi.getRemoved()) {
                ClassMapping relatedClassMapping = getClassMapping().getSchemaMapping().getClassMapping(removed.getClass().getName());
                Row row = createReferencedRow(relatedClassMapping, removed);
                for (JoinColumn joinColumn : joinColumns) {
                    row.setProperty(joinColumn.getReferencedUpdateColumn(), null);
                }
                PreparedPersistorStatement ps = persistor.getCollectionWithoutReferenceUpdateStatement(this, row.getTable(), row.getSetColumns());
                ps.addBatch(row, removed);
            }
            for (Object added : cmi.getAdded()) {
                ClassMapping relatedClassMapping = getClassMapping().getSchemaMapping().getClassMapping(added.getClass().getName());
                Row row = createReferencedRow(relatedClassMapping, added);
                for (JoinColumn joinColumn : joinColumns) {
                    ColumnMapping cm = getClassMapping().getColumnMapping(joinColumn.getMyReadColumn());
                    Object myValue = ModifiableAccessor.Singleton.getValue(persistor.getModificationInfo().getObject(), cm.getPropertyDescription());
                    row.setProperty(joinColumn.getReferencedUpdateColumn(), myValue);
                }
                PreparedPersistorStatement ps = persistor.getCollectionWithoutReferenceUpdateStatement(this, row.getTable(), row.getSetColumns());
                ps.addBatch(row, added);
            }
        }
    }

    private Row createReferencedRow(ClassMapping relatedClassMapping, Object object) {
        Row row = new Row(relatedClassMapping.getPrimaryUpdateTable());
        for (ColumnMapping columnMapping : relatedClassMapping.getPrimaryKey().getColumnMappings()) {
            // Todo: should be ModifiableAccessor from related object persistor
            Object value = ModifiableAccessor.Singleton.getValue(object, columnMapping.getPropertyDescription());
            row.setProperty(columnMapping.getUpdateColumn(), value);
        }
        return row;
    }

    @Override
    public void visitColumns(ColumnVisitor visitor) {
        for (JoinColumn joinColumn : joinColumns) {
            visitor.visit(joinColumn.getReferencedReadColumn());
        }
    }

}
