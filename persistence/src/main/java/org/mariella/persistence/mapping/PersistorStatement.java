package org.mariella.persistence.mapping;

import org.mariella.persistence.database.ParameterValues;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.persistor.Persistor;
import org.mariella.persistence.persistor.Row;

import java.util.function.Consumer;

public interface PersistorStatement {

    <T extends PreparedPersistorStatement> T prepare(Persistor<T> persistor);

    Consumer<RowAndObject> getGeneratedColumnsCallback();

    void setParameters(ParameterValues parameterValues, Row row);

    String getSqlString();

    String getSqlDebugString(Row parameters);
}
