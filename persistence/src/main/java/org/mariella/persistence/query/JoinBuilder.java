package org.mariella.persistence.query;

import org.mariella.persistence.database.Column;

import java.util.List;

public interface JoinBuilder {
    JoinType getJoinType();

    void setJoinType(JoinType joinType);

    void createJoin();

    TableReference getJoinedTableReference();

    ConditionBuilder getConditionBuilder(Column column);

    List<Expression> getOrderBy();

    boolean isAddToOrderBy();

    void setAddToOrderBy(boolean addToOrderBy);

    enum JoinType {
        inner,
        leftouter,
        rightouter
    }
}
