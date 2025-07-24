package org.mariella.persistence.database;

public record Column(String name, int type, boolean nullable, Converter<?> converter) {

    public Column {
        if (converter == null) {
            throw new NullPointerException("converter is null for column " + name + " and type " + type);
        }
    }

    @SuppressWarnings("unchecked")
    public void setObject(ParameterValues pv, int index, Object value) {
        ((Converter<? super Object>) converter).setObject(pv, index, value);
    }

    public Object getObject(ResultRow rs, int index) {
        return converter.getObject(rs, index);
    }

    @Override
    public String toString() {
        return name;
    }

}
