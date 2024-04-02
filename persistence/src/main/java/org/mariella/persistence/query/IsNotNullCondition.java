package org.mariella.persistence.query;

public record IsNotNullCondition(Expression expression) implements Expression {

    @Override
    public void printSql(StringBuilder b) {
        expression.printSql(b);
        b.append(" IS NOT NULL");
    }

}
