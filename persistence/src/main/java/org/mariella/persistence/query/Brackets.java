package org.mariella.persistence.query;

public class Brackets implements Expression {
    private final Expression expression;

    public Brackets(Expression expression) {
        super();
        this.expression = expression;
    }

    public void printSql(StringBuilder b) {
        b.append("(");
        expression.printSql(b);
        b.append(")");
    }

}
