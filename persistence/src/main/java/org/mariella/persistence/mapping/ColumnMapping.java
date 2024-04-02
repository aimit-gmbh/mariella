package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.database.ResultSetReader;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.query.SubSelectBuilder;
import org.mariella.persistence.query.TableReference;
import org.mariella.persistence.schema.PropertyDescription;
import org.mariella.persistence.util.Util;

import java.util.Collection;

public class ColumnMapping extends PhysicalPropertyMapping {
    protected final boolean insertable;
    protected final boolean updatable;
    private final Column readColumn;
    private final Column updateColumn;
    private ColumnValueGenerator valueGenerator;

    public ColumnMapping(ClassMapping classMapping, PropertyDescription propertyDescription, boolean insertable,
                         boolean updatable, Column readColumn, Column updateColumn) {
        super(classMapping, propertyDescription);
        this.insertable = insertable;
        this.updatable = updatable;
        this.readColumn = readColumn;
        this.updateColumn = updateColumn;
        Util.assertTrue(readColumn != null, "Unknown column");
        if (insertable || updatable) {
            Util.assertTrue(updateColumn != null, "Unknown column");
        }
    }

    @Override
    public boolean isInsertable() {
        return insertable;
    }

    @Override
    public boolean isUpdatable() {
        return updatable;
    }

    public Column getReadColumn() {
        return readColumn;
    }

    public Column getUpdateColumn() {
        return updateColumn;
    }

    public ColumnValueGenerator getValueGenerator() {
        return valueGenerator;
    }

    public void setValueGenerator(ColumnValueGenerator valueGenerator) {
        this.valueGenerator = valueGenerator;
    }

    @Override
    public ClassMapping getClassMapping() {
        return (ClassMapping) super.getClassMapping();
    }

    @Override
    public void insertPrimary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        if (!insertable) {
            throw new IllegalStateException("Not insertable!");
        }
        if (value != null || (valueGenerator != null && !valueGenerator.isGeneratedByDatabase())) {
            super.insertPrimary(persistor, value);
        }
    }

    @Override
    public void insertSecondary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        if (!insertable) {
            throw new IllegalStateException("Not insertable!");
        }
        super.insertSecondary(persistor, value);
    }

    @Override
    public void updatePrimary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        if (!updatable) {
            throw new IllegalStateException("Not updatable!");
        }
        super.updatePrimary(persistor, value);
    }

    @Override
    public void updateSecondary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        if (!updatable) {
            throw new IllegalStateException("Not updatable!");
        }
        super.updateSecondary(persistor, value);
    }

    @Override
    public void persistPrimary(ObjectPersistor<? extends PreparedPersistorStatement> persistor, Object value) {
        getClassMapping().getPrimaryRow(persistor, this).setProperty(getUpdateColumn(), value);
    }

    @Override
    public void collectUsedColumns(Collection<Column> collection) {
        if (!collection.contains(readColumn)) {
            collection.add(readColumn);
        }
        if (updateColumn != null && !collection.contains(updateColumn)) {
            collection.add(updateColumn);
        }
    }

    @Override
    public Object getObject(ResultSetReader reader, ObjectFactory factory) {
        Object value = readColumn.getObject(reader.getResultRow(), reader.getCurrentColumnIndex());
        advance(reader);
        return value;
    }

    @Override
    public void advance(ResultSetReader reader) {
        reader.setCurrentColumnIndex(reader.getCurrentColumnIndex() + 1);
    }

    @Override
    public void addColumns(SubSelectBuilder subSelectBuilder, TableReference tableReference) {
        subSelectBuilder.addSelectItem(tableReference, getReadColumn());
    }

    @Override
    public void visitColumns(ColumnVisitor visitor) {
        visitor.visit(readColumn);
    }

}
