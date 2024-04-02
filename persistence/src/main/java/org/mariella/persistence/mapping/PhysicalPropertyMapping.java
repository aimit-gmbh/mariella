package org.mariella.persistence.mapping;

import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.database.ResultSetReader;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.query.SubSelectBuilder;
import org.mariella.persistence.query.TableReference;
import org.mariella.persistence.schema.PropertyDescription;

import java.sql.SQLException;

public abstract class PhysicalPropertyMapping extends PropertyMapping {

    public PhysicalPropertyMapping(ClassMapping classMapping, PropertyDescription propertyDescription) {
        super(classMapping, propertyDescription);
    }

    @Override
    protected abstract void persistPrimary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value);

    @Override
    protected void persistSecondary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
    }

    public abstract Object getObject(ResultSetReader reader, ObjectFactory factory);

    public abstract void advance(ResultSetReader reader) throws SQLException;

    public abstract void addColumns(SubSelectBuilder subSelectBuilder, TableReference tableReference);

}
