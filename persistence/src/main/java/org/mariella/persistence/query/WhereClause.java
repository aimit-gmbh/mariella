package org.mariella.persistence.query;


public class WhereClause implements Expression {
    private Expression condition;

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public boolean isEmpty() {
        return condition == null;
    }

    public void printSql(StringBuilder b) {
        b.append("WHERE ");
        if (condition != null) {
            condition.printSql(b);
        }
    }

}
