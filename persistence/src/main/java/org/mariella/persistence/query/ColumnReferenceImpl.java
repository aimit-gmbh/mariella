package org.mariella.persistence.query;


import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Converter;

public record ColumnReferenceImpl(Expression tableReference, Column column) implements ColumnReference {

    @Override
    @SuppressWarnings("unchecked")
    public <T> Converter<T> getConverter() {
        return (Converter<T>) column.converter();
    }

    public void printSql(StringBuilder b) {
        tableReference.printSql(b);
        b.append('.');
        b.append(column.name());
    }

}
