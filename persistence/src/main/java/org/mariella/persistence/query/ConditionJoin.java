package org.mariella.persistence.query;

public abstract class ConditionJoin extends Join {
    private Expression condition;

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    @Override
    public void printFromClause(StringBuilder b) {
        super.printFromClause(b);
        if (condition != null) {
            b.append(" ON ");
            condition.printSql(b);
        }
    }

}
