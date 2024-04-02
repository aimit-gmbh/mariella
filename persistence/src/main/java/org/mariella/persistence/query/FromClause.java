package org.mariella.persistence.query;

public class FromClause implements Expression {
    private FromClauseElement expression;

    public FromClauseElement getExpression() {
        return expression;
    }

    public void setExpression(FromClauseElement expression) {
        this.expression = expression;
    }

    public void printSql(StringBuilder b) {
        b.append("FROM ");
        expression.printFromClause(b);
    }

}
