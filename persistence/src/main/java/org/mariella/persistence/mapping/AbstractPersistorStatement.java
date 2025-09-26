package org.mariella.persistence.mapping;

import org.mariella.persistence.database.*;
import org.mariella.persistence.persistor.Row;

import java.util.function.Consumer;

public abstract class AbstractPersistorStatement implements PersistorStatement {

    protected final Schema schema;
    protected final Table table;
    protected int nextParameterIndex = 1;

    public AbstractPersistorStatement(Schema schema, Table table) {
        this.schema = schema;
        this.table = table;
    }

    protected Parameter createParameter() {
        return schema.createParameter(nextParameterIndex++);
    }

    protected abstract String getSqlString(BuildCallback buildCallback);

    @Override
    public String getSqlString() {
        nextParameterIndex = 1;
        return getSqlString((b, column) -> schema.createParameter(nextParameterIndex++).print(b));
    }

    @Override
    public String getSqlDebugString(Row parameters) {
        return getSqlString(
                (b, column) -> {
                    @SuppressWarnings("unchecked")
                    Converter<Object> converter = (Converter<Object>) column.converter();
                    b.append(converter.toString(parameters.getProperty(column)));
                });
    }

    @Override
    public Consumer<RowAndObject> getGeneratedColumnsCallback() {
        return null;
    }

    protected interface BuildCallback {
        void columnValue(StringBuilder b, Column column);
    }
}
