package org.mariella.persistence.database;

import org.mariella.persistence.query.Literal;


public interface Converter<T> {
    void setObject(ParameterValues pv, int index, T value);

    T getObject(ResultRow row, int index);

    Literal<T> createLiteral(Object value);

    Literal<T> createDummy();

    String toString(T value);
}
