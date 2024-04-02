package org.mariella.persistence.query;

import org.mariella.persistence.database.Column;

import java.util.List;


public class RelationshipAsTableJoinBuilder implements JoinBuilder {
    private final CreateContentJoinBuilderCallback callback;

    private final JoinBuilder joinTableJoinBuilder;
    private JoinBuilder contentJoinBuilder;

    public RelationshipAsTableJoinBuilder(JoinBuilder joinTableJoinBuilder, CreateContentJoinBuilderCallback callback) {
        super();
        this.callback = callback;
        this.joinTableJoinBuilder = joinTableJoinBuilder;
    }

    public JoinBuilder getJoinTableJoinBuilder() {
        return joinTableJoinBuilder;
    }

    @Override
    public TableReference getJoinedTableReference() {
        return contentJoinBuilder == null ? null : contentJoinBuilder.getJoinedTableReference();
    }

    @Override
    public void createJoin() {
        joinTableJoinBuilder.createJoin();
        contentJoinBuilder = callback.createContentJoinBuilder(joinTableJoinBuilder);
        contentJoinBuilder.setJoinType(joinTableJoinBuilder.getJoinType());
        contentJoinBuilder.setAddToOrderBy(joinTableJoinBuilder.isAddToOrderBy());
        contentJoinBuilder.createJoin();
    }

    @Override
    public ConditionBuilder getConditionBuilder(Column column) {
        return contentJoinBuilder.getConditionBuilder(column);
    }

    @Override
    public JoinType getJoinType() {
        return joinTableJoinBuilder.getJoinType();
    }

    @Override
    public void setJoinType(JoinType joinType) {
        joinTableJoinBuilder.setJoinType(joinType);
    }

    @Override
    public List<Expression> getOrderBy() {
        return contentJoinBuilder.getOrderBy();
    }

    @Override
    public boolean isAddToOrderBy() {
        return joinTableJoinBuilder.isAddToOrderBy();
    }

    @Override
    public void setAddToOrderBy(boolean addToOrderBy) {
        joinTableJoinBuilder.setAddToOrderBy(addToOrderBy);
    }

    public interface CreateContentJoinBuilderCallback {
        JoinBuilder createContentJoinBuilder(JoinBuilder joinTableJoinBuilder);
    }

}
