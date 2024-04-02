package org.mariella.persistence.query;

import org.mariella.persistence.database.Column;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractJoinBuilder implements JoinBuilder {
    protected final SubSelectBuilder subSelectBuilder;
    protected final ConditionBuilder conditionBuilder = new ConditionBuilder();
    protected final List<Expression> orderBy = new ArrayList<>();

    private boolean addToOrderBy = false;

    public AbstractJoinBuilder(SubSelectBuilder subSelectBuilder) {
        super();
        this.subSelectBuilder = subSelectBuilder;
    }

    @Override
    public boolean isAddToOrderBy() {
        return addToOrderBy;
    }

    @Override
    public void setAddToOrderBy(boolean addToOrderBy) {
        this.addToOrderBy = addToOrderBy;
    }

    public abstract JoinType getJoinType();

    public SubSelectBuilder getSubSelectBuilder() {
        return subSelectBuilder;
    }

    @Override
    public ConditionBuilder getConditionBuilder(Column column) {
        return conditionBuilder;
    }

    @Override
    public List<Expression> getOrderBy() {
        return orderBy;
    }

}
