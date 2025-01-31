package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.query.BinaryCondition;
import org.mariella.persistence.query.JoinBuilder;
import org.mariella.persistence.query.JoinBuilder.JoinType;
import org.mariella.persistence.query.SubSelectBuilder;
import org.mariella.persistence.query.TableReference;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.schema.PropertyDescription;
import org.mariella.persistence.schema.ReferencePropertyDescription;

import java.util.Collection;
import java.util.List;

public class ReferencePropertyMapping extends RelationshipPropertyMapping {
    private List<JoinColumn> joinColumns = null;

    public ReferencePropertyMapping(ClassMapping classMapping, PropertyDescription propertyDescription) {
        super(classMapping, (ReferencePropertyDescription) propertyDescription);
    }

    public List<JoinColumn> getJoinColumns() {
        return joinColumns;
    }

    public void setJoinColumns(List<JoinColumn> joinColumns) {
        this.joinColumns = joinColumns;
    }

    @Override
    public ReferencePropertyDescription getPropertyDescription() {
        return (ReferencePropertyDescription) super.getPropertyDescription();
    }

    @Override
    public JoinBuilder createJoinBuilder(SubSelectBuilder subSelectBuilder, TableReference myTableReference) {
        if (joinColumns != null) {
            JoinBuilder joinBuilder = getReferencedClassMapping().createJoinBuilder(subSelectBuilder);
            for (JoinColumn joinColumn : joinColumns) {
                joinBuilder.getConditionBuilder(joinColumn.getReferencedReadColumn()).and(
                        BinaryCondition.eq(
                                myTableReference.createColumnReferenceForRelationship(joinColumn.getMyReadColumn()),
                                joinBuilder.getJoinedTableReference()
                                        .createColumnReference(joinColumn.getReferencedReadColumn())));
                joinBuilder.setJoinType(joinColumn.getMyReadColumn().nullable() ? JoinType.leftouter : JoinType.inner);
            }
            return joinBuilder;
        } else {
            return getReversePropertyMapping().createReverseJoinBuilder(subSelectBuilder, myTableReference);
        }
    }

    @Override
    protected JoinBuilder createReverseJoinBuilder(SubSelectBuilder subSelectBuilder, TableReference referencedTableReference) {
        if (joinColumns == null) {
            throw new UnsupportedOperationException();
        } else {
            JoinBuilder joinBuilder = getClassMapping().createJoinBuilder(subSelectBuilder);
            for (JoinColumn joinColumn : joinColumns) {
                joinBuilder.getConditionBuilder(joinColumn.getMyReadColumn()).and(
                        BinaryCondition.eq(
                                joinBuilder.getJoinedTableReference()
                                        .createColumnReferenceForRelationship(joinColumn.getMyReadColumn()),
                                referencedTableReference.createColumnReference(joinColumn.getReferencedReadColumn())));
            }
            return joinBuilder;
        }
    }

    @Override
    protected void persistPrimary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void persistSecondary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
    }

    @Override
    public void insertPrimary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        persist(persistor, value, true);
    }

    @Override
    public void updatePrimary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        persist(persistor, value, false);
    }

    protected void persist(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value, boolean isInsert) {
        if (joinColumns != null) {
            for (JoinColumn joinColumn : joinColumns) {
                if (isInsert && joinColumn.isInsertable() || !isInsert && joinColumn.isUpdatable()) {
                    Object relatedValue = null;
                    if (value != null) {
                        ColumnMapping cm = getReferencedClassMapping().getColumnMapping(joinColumn.getReferencedUpdateColumn());
                        relatedValue = ModifiableAccessor.Singleton.getValue(value, cm.getPropertyDescription());
                    }
                    getClassMapping().getPrimaryRow(persistor, this).setProperty(joinColumn.getMyUpdateColumn(), relatedValue);
                }
            }
        }
    }

    @Override
    public void collectUsedColumns(Collection<Column> collection) {
        if (joinColumns != null) {
            for (JoinColumn joinColumn : joinColumns) {
                if (!collection.contains(joinColumn.getMyReadColumn())) {
                    collection.add(joinColumn.getMyReadColumn());
                }
                if (!collection.contains(joinColumn.getMyUpdateColumn())) {
                    collection.add(joinColumn.getMyUpdateColumn());
                }
            }
        }
    }

    @Override
    public void visitColumns(ColumnVisitor visitor) {
        for (JoinColumn joinColumn : joinColumns) {
            visitor.visit(joinColumn.getMyReadColumn());
        }
    }

}
