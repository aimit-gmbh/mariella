package org.mariella.persistence.query;

public record IsNullCondition(Expression expression) implements Expression {

    @Override
    public void printSql(StringBuilder b) {
        expression.printSql(b);
        b.append(" IS NULL");
    }

}
