package org.mariella.persistence.query;

import java.util.ArrayList;
import java.util.List;

public class OrderByClause implements Expression {
    private final List<Expression> items = new ArrayList<>();

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<Expression> getItems() {
        return items;
    }

    public void printSql(StringBuilder b) {
        b.append("ORDER BY ");
        boolean first = true;
        for (Expression item : items) {
            if (first)
                first = false;
            else
                b.append(", ");
            item.printSql(b);
        }
    }

}
