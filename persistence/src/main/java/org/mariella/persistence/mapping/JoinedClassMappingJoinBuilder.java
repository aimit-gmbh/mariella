package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.query.ConditionBuilder;
import org.mariella.persistence.query.Expression;
import org.mariella.persistence.query.JoinBuilder;
import org.mariella.persistence.query.TableReference;

import java.util.ArrayList;
import java.util.List;

public class JoinedClassMappingJoinBuilder implements JoinBuilder {
    private final JoinedClassMappingTableReference tableReference = new JoinedClassMappingTableReference();

    private final List<JoinBuilder> joinBuilders = new ArrayList<>();

    public JoinedClassMappingJoinBuilder() {
        super();
    }

    @Override
    public boolean isAddToOrderBy() {
        return getPrimaryJoinBuilder().isAddToOrderBy();
    }

    @Override
    public void setAddToOrderBy(boolean addToOrderBy) {
        getPrimaryJoinBuilder().setAddToOrderBy(addToOrderBy);
    }

    public void addJoinBuilder(JoinBuilder joinBuilder) {
        joinBuilders.add(joinBuilder);
        tableReference.addTableReference(joinBuilder.getJoinedTableReference());
    }

    @Override
    public void createJoin() {
        for (JoinBuilder joinBuilder : joinBuilders) {
            joinBuilder.createJoin();
        }
    }

    @Override
    public JoinType getJoinType() {
        return getPrimaryJoinBuilder().getJoinType();
    }

    @Override
    public void setJoinType(JoinType joinType) {
        getPrimaryJoinBuilder().setJoinType(joinType);
    }

    public JoinBuilder getPrimaryJoinBuilder() {
        return joinBuilders.get(0);
    }

    @Override
    public ConditionBuilder getConditionBuilder(Column column) {
        for (JoinBuilder joinBuilder : joinBuilders) {
            if (joinBuilder.getJoinedTableReference().canCreateColumnReference(column)) {
                return joinBuilder.getConditionBuilder(column);
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public TableReference getJoinedTableReference() {
        return tableReference;
    }

    @Override
    public List<Expression> getOrderBy() {
        return getPrimaryJoinBuilder().getOrderBy();
    }

}
