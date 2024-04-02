package org.mariella.persistence.query;

import org.mariella.persistence.database.Converter;

public interface ScalarExpression extends Expression {
    <T> Converter<T> getConverter();
}
