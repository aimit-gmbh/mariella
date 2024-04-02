package org.mariella.persistence.query;

public class LeftOuterJoin extends ConditionJoin {

    @Override
    protected void printJoin(StringBuilder b) {
        b.append(" LEFT OUTER JOIN ");
    }

    @Override
    public void printFromClause(StringBuilder b) {
        if (getRight() instanceof TableReference && !((TableReference) getRight()).isReferenced()) {
            getLeft().printFromClause(b);
        } else {
            super.printFromClause(b);
        }
    }

}
