package org.mariella.persistence.query;

public class SecondaryTableJoin extends ConditionJoin {
    private boolean isReferencedByForeignKey = false;

    public void markReferencedByForeignKey() {
        isReferencedByForeignKey = true;
    }

    @Override
    protected void printJoin(StringBuilder b) {
        if (isReferencedByForeignKey) {
            b.append(" INNER JOIN ");
        } else {
            b.append(" LEFT OUTER JOIN ");
        }
    }


}
