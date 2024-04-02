package org.mariella.persistence.query;

import org.mariella.persistence.database.Table;

public class JoinBuilderImpl extends AbstractJoinBuilder {
    private JoinType joinType = JoinType.inner;
    private TableReference joinedTableReference;

    public JoinBuilderImpl(SubSelectBuilder subSelectBuilder) {
        super(subSelectBuilder);
    }

    public JoinBuilderImpl(SubSelectBuilder subSelectBuilder, Table table) {
        super(subSelectBuilder);
        joinedTableReference = subSelectBuilder.createJoinedTable(table);
    }

    @Override
    public TableReference getJoinedTableReference() {
        return joinedTableReference;
    }

    public void setJoinedTableReference(TableReference joinedTableReference) {
        this.joinedTableReference = joinedTableReference;
    }

    @Override
    public JoinType getJoinType() {
        return joinType;
    }

    @Override
    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    @Override
    public void createJoin() {
        switch (getJoinType()) {
            case inner -> createInnerJoin();
            case leftouter -> createLeftOuterJoin();
            case rightouter -> createRightOuterJoin();
        }
        if (isAddToOrderBy()) {
            for (Expression e : orderBy) {
                subSelectBuilder.addOrderBy(e);
            }
        }
    }

    private void createLeftOuterJoin() {
        if (getSubSelectBuilder().getSubSelect().getFromClause().getExpression() == null) {
            throw new IllegalStateException("Cannot left outer join nothing");
        } else {
            LeftOuterJoin join = new LeftOuterJoin();
            join.setLeft(getSubSelectBuilder().getSubSelect().getFromClause().getExpression());
            join.setRight(getJoinedTableReference());
            join.setCondition(conditionBuilder.getCondition());
            getSubSelectBuilder().getSubSelect().getFromClause().setExpression(join);
            getSubSelectBuilder().getJoinedTableReferences().add(getJoinedTableReference());
        }
    }

    private void createRightOuterJoin() {
        if (getSubSelectBuilder().getSubSelect().getFromClause().getExpression() == null) {
            throw new IllegalStateException("Cannot right outer join nothing");
        } else {
            RightOuterJoin join = new RightOuterJoin();
            join.setLeft(getSubSelectBuilder().getSubSelect().getFromClause().getExpression());
            join.setRight(getJoinedTableReference());
            join.setCondition(conditionBuilder.getCondition());
            getSubSelectBuilder().getSubSelect().getFromClause().setExpression(join);
            getSubSelectBuilder().getJoinedTableReferences().add(getJoinedTableReference());
        }
    }

    private void createInnerJoin() {
        if (conditionBuilder.getCondition() == null) {
            if (getSubSelectBuilder().getSubSelect().getFromClause().getExpression() == null) {
                getSubSelectBuilder().getSubSelect().getFromClause().setExpression(getJoinedTableReference());
            } else {
                InnerJoin join = new InnerJoin();
                join.setLeft(getSubSelectBuilder().getSubSelect().getFromClause().getExpression());
                join.setRight(getJoinedTableReference());
                getSubSelectBuilder().getSubSelect().getFromClause().setExpression(join);
            }
            getSubSelectBuilder().getJoinedTableReferences().add(getJoinedTableReference());
            if (conditionBuilder.getCondition() != null) {
                subSelectBuilder.and(conditionBuilder.getCondition());
            }
        } else {
            if (getSubSelectBuilder().getSubSelect().getFromClause().getExpression() == null) {
                getSubSelectBuilder().getSubSelect().getFromClause().setExpression(getJoinedTableReference());
                getSubSelectBuilder().and(conditionBuilder.getCondition());
            } else {
                ConditionalInnerJoin join = new ConditionalInnerJoin();
                join.setLeft(getSubSelectBuilder().getSubSelect().getFromClause().getExpression());
                join.setRight(getJoinedTableReference());
                join.setCondition(conditionBuilder.getCondition());
                getSubSelectBuilder().getSubSelect().getFromClause().setExpression(join);
            }
            getSubSelectBuilder().getJoinedTableReferences().add(getJoinedTableReference());
        }
    }

}
