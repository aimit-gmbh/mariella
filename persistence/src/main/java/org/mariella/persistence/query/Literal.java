package org.mariella.persistence.query;

import org.mariella.persistence.database.Converter;

public class Literal<T> implements ScalarExpression {
    protected final Converter<T> converter;
    protected final T value;

    public Literal(Converter<T> converter, T value) {
        this.converter = converter;
        this.value = value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T1> Converter<T1> getConverter() {
        return (Converter<T1>) converter;
    }

    public void printSql(StringBuilder b) {
        b.append(converter.toString(value));
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        printSql(b);
        return b.toString();
    }
}
