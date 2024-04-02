package org.mariella.persistence.query;

public class RightOuterJoin extends ConditionJoin {

    @Override
    protected void printJoin(StringBuilder b) {
        b.append(" RIGHT OUTER JOIN ");
    }

}
