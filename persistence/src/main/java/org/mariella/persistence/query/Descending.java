package org.mariella.persistence.query;

public class Descending implements Expression {
    private final Expression columnReference;

    public Descending(Expression columnReference) {
        this.columnReference = columnReference;
    }

    @Override
    public void printSql(StringBuilder b) {
        columnReference.printSql(b);
        b.append(" DESC");
    }

}
