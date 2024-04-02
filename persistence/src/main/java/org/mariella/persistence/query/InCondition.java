package org.mariella.persistence.query;

import java.util.List;

public class InCondition implements Expression {

    private final Expression leftExpression;
    private final List<Expression> in;
    private int maxInExpressions = 1000;

    public InCondition(Expression leftExpression, List<Expression> in) {
        super();
        this.leftExpression = leftExpression;
        this.in = in;
    }

    public int getMaxInExpressions() {
        return maxInExpressions;
    }

    public void setMaxInExpressions(int maxInExpressions) {
        if (maxInExpressions <= 0) {
            throw new IllegalArgumentException();
        }
        this.maxInExpressions = maxInExpressions;
    }

    public void printSql(StringBuilder b) {
        if (in.size() > maxInExpressions) {
            b.append("(");
        }
        leftExpression.printSql(b);
        b.append(" IN (");
        int i = 0;
        for (Expression expression : in) {
            if (i == maxInExpressions) {
                b.append(") OR ");
                leftExpression.printSql(b);
                b.append(" IN (");
                i = 0;
            } else if (i > 0) {
                b.append(", ");
            }
            expression.printSql(b);
            i++;
        }
        b.append(in.size() > maxInExpressions ? "))" : ")");
    }

}
