package org.mariella.persistence.query;

public class ConditionBuilder {
    private Expression condition;

    public void and(Expression expression) {
        if (condition == null) {
            condition = expression;
        } else {
            condition = BinaryCondition.and(condition, expression);
        }
    }

    public Expression getCondition() {
        return condition;
    }

}
