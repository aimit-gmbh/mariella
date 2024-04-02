package org.mariella.persistence.query;


public class SecondaryTableJoinBuilder extends AbstractJoinBuilder {
    private final JoinType joinType = JoinType.leftouter;
    private JoinedSecondaryTable secondaryTable;

    public SecondaryTableJoinBuilder(SubSelectBuilder subSelectBuilder) {
        super(subSelectBuilder);
    }

    @Override
    public JoinType getJoinType() {
        return joinType;
    }

    @Override
    public void setJoinType(JoinType joinType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TableReference getJoinedTableReference() {
        return secondaryTable;
    }

    public JoinedSecondaryTable getSecondaryTable() {
        return secondaryTable;
    }

    public void setSecondaryTable(JoinedSecondaryTable secondaryTable) {
        this.secondaryTable = secondaryTable;
    }

    @Override
    public void createJoin() {
        if (getSubSelectBuilder().getSubSelect().getFromClause().getExpression() == null) {
            throw new IllegalStateException("Cannot left outer join a secondary table on nothing");
        } else {
            SecondaryTableJoin join = new SecondaryTableJoin();
            join.setLeft(getSubSelectBuilder().getSubSelect().getFromClause().getExpression());
            join.setRight(getJoinedTableReference());
            join.setCondition(conditionBuilder.getCondition());
            getSubSelectBuilder().getSubSelect().getFromClause().setExpression(join);
            getSubSelectBuilder().getJoinedTableReferences().add(getJoinedTableReference());
            secondaryTable.setJoin(join);
        }
    }

}
