package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.query.JoinBuilder;
import org.mariella.persistence.query.SubSelectBuilder;
import org.mariella.persistence.query.TableReference;
import org.mariella.persistence.schema.CollectionPropertyDescription;
import org.mariella.persistence.schema.PropertyDescription;


public class CollectionPropertyMapping extends RelationshipPropertyMapping {
    private Column orderColumn;

    public CollectionPropertyMapping(ClassMapping classMapping, PropertyDescription propertyDescription) {
        super(classMapping, (CollectionPropertyDescription) propertyDescription);
    }

    public CollectionPropertyMapping(ClassMapping classMapping, PropertyDescription propertyDescription, Column orderColumn) {
        super(classMapping, (CollectionPropertyDescription) propertyDescription);
        this.orderColumn = orderColumn;
    }

    @Override
    public CollectionPropertyDescription getPropertyDescription() {
        return (CollectionPropertyDescription) super.getPropertyDescription();
    }

    @Override
    public JoinBuilder createJoinBuilder(SubSelectBuilder subSelectBuilder, TableReference myTableReference) {
        JoinBuilder joinBuilder = getReversePropertyMapping().createReverseJoinBuilder(subSelectBuilder, myTableReference);
        if (orderColumn != null) {
            joinBuilder.getOrderBy().add(joinBuilder.getJoinedTableReference().createColumnReference(orderColumn));
        }
        return joinBuilder;
    }

    @Override
    protected JoinBuilder createReverseJoinBuilder(SubSelectBuilder subSelectBuilder, TableReference referencedTableReference) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void persistPrimary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
    }

    @Override
    protected void persistSecondary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
    }

    @Override
    public void visitColumns(ColumnVisitor visitor) {
    }

}
