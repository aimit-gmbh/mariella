package org.mariella.persistence.database;

import org.mariella.persistence.query.Literal;


@SuppressWarnings("rawtypes")
public class EnumConverter<T extends Enum> implements Converter<T> {
    private final Class<T> enumClass;

    public EnumConverter(Class<T> enumClass) {
        super();
        this.enumClass = enumClass;
    }

    @SuppressWarnings("unchecked")
    protected T getEnumValue(String string) {
        return (T) Enum.valueOf(enumClass, string);
    }

    protected String getString(T value) {
        return value == null ? null : value.toString();
    }

    public T getObject(ResultRow row, int index) {
        String value = row.getString(index);
        if (value == null) {
            return null;
        } else {
            return getEnumValue(value);
        }
    }

    public void setObject(ParameterValues pv, int index, T value) {
        String string = getString(value);
        pv.setString(index, string);
    }

    public Literal<T> createLiteral(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Literal<T> createDummy() {
        return new Literal<>(this, null) {
            @Override
            public void printSql(StringBuilder b) {
                b.append("''");
            }
        };
    }

    public String toString(T value) {
        String string = getString(value);
        return string == null ? "null" : string;
    }

}
