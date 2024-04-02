package org.mariella.persistence.mapping;

import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.database.ResultSetReader;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.query.SubSelectBuilder;
import org.mariella.persistence.query.TableReference;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.schema.PropertyDescription;

import java.sql.SQLException;


public class EmbeddedPropertyMapping extends PhysicalPropertyMapping {
    protected final EmbeddedClassMapping embeddedClassMapping;

    public EmbeddedPropertyMapping(ClassMapping classMapping, EmbeddedClassMapping embeddedClassMapping,
                                   PropertyDescription propertyDescription) {
        super(classMapping, propertyDescription);
        this.embeddedClassMapping = embeddedClassMapping;
    }

    @Override
    public void addColumns(SubSelectBuilder subSelectBuilder, TableReference tableReference) {
        for (PhysicalPropertyMapping pm : embeddedClassMapping.getPhysicalPropertyMappingList()) {
            pm.addColumns(subSelectBuilder, tableReference);
        }
    }

    @Override
    public void visitColumns(ColumnVisitor visitor) {
        for (PhysicalPropertyMapping pm : embeddedClassMapping.getPhysicalPropertyMappingList()) {
            pm.visitColumns(visitor);
        }
    }

    @Override
    public void advance(ResultSetReader reader) throws SQLException {
        for (PhysicalPropertyMapping pm : embeddedClassMapping.getPhysicalPropertyMappingList()) {
            pm.advance(reader);
        }
    }

    @Override
    public Object getObject(ResultSetReader reader, ObjectFactory factory) {
        return embeddedClassMapping.createObject(reader, factory);
    }

    @Override
    protected void persistPrimary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        for (PhysicalPropertyMapping pm : embeddedClassMapping.getPhysicalPropertyMappingList()) {
            Object propertyValue = ModifiableAccessor.Singleton.getValue(value, pm.getPropertyDescription());
            pm.persistPrimary(persistor, propertyValue);
        }
    }

    @Override
    protected void persistSecondary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        for (PhysicalPropertyMapping pm : embeddedClassMapping.getPhysicalPropertyMappingList()) {
            Object propertyValue = ModifiableAccessor.Singleton.getValue(value, pm.getPropertyDescription());
            pm.persistSecondary(persistor, propertyValue);
        }
    }

}
