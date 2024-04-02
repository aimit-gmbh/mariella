package org.mariella.persistence.query;

import org.mariella.persistence.database.Converter;

public class SelectItem implements ScalarExpression {
    private String alias;
    private Expression columnReference;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Expression getColumnReference() {
        return columnReference;
    }

    public void setColumnReference(Expression columnReference) {
        this.columnReference = columnReference;
    }

    @Override
    public <T> Converter<T> getConverter() {
        return columnReference instanceof ScalarExpression ? ((ScalarExpression) columnReference).getConverter() : null;
    }

    public void printSql(StringBuilder b) {
        columnReference.printSql(b);
        if (alias != null) {
            b.append(' ');
            b.append(alias);
        }
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        printSql(b);
        return b.toString();
    }

}
