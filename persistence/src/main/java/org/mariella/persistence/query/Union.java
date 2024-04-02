package org.mariella.persistence.query;

public class Union implements Expression {
    private boolean all = true;
    private Expression leftExpression;
    private Expression rightExpression;

    public Union(Expression leftExpression, Expression righExpression) {
        super();
        this.leftExpression = leftExpression;
        this.rightExpression = righExpression;
    }

    public void printSql(StringBuilder b) {
        leftExpression.printSql(b);
        b.append("\nunion");
        if (all) {
            b.append(" all");
        }
        b.append("\n");
        rightExpression.printSql(b);
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    public Expression getRightExpression() {
        return rightExpression;
    }

    public void setRightExpression(Expression rightExpression) {
        this.rightExpression = rightExpression;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

}
