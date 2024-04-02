package org.mariella.persistence.query;

public class SubSelect implements Expression {
    private final SelectClause selectClause = new SelectClause();
    private final FromClause fromClause = new FromClause();
    private final WhereClause whereClause = new WhereClause();
    private final GroupByClause groupByClause = new GroupByClause();
    private final OrderByClause orderByClause = new OrderByClause();

    public SelectClause getSelectClause() {
        return selectClause;
    }

    public FromClause getFromClause() {
        return fromClause;
    }

    public WhereClause getWhereClause() {
        return whereClause;
    }

    public GroupByClause getGroupByClause() {
        return groupByClause;
    }

    public OrderByClause getOrderByClause() {
        return orderByClause;
    }

    public void printSql(StringBuilder b) {
        selectClause.printSql(b);
        b.append(' ');
        fromClause.printSql(b);
        if (!whereClause.isEmpty()) {
            b.append(' ');
            whereClause.printSql(b);
        }
        if (!groupByClause.isEmpty()) {
            b.append(' ');
            groupByClause.printSql(b);
        }
        if (!orderByClause.isEmpty()) {
            b.append(' ');
            orderByClause.printSql(b);
        }
    }

    public String toSqlString() {
        StringBuilder b = new StringBuilder();
        printSql(b);
        return b.toString();
    }

}

