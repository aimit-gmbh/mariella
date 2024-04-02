package org.mariella.persistence.query;

import java.util.ArrayList;
import java.util.List;

public class GroupByClause implements Expression {
    private final List<Expression> items = new ArrayList<>();
    private Expression having;

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<Expression> getItems() {
        return items;
    }

    public Expression getHaving() {
        return having;
    }

    public void setHaving(Expression having) {
        this.having = having;
    }

    public void printSql(StringBuilder b) {
        b.append("GROUP BY ");
        boolean first = true;
        for (Expression item : items) {
            if (first)
                first = false;
            else
                b.append(", ");
            item.printSql(b);
        }
        if (having != null) {
            b.append(" HAVING ");
            having.printSql(b);
        }
    }

}
