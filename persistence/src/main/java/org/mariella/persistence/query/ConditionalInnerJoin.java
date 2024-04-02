package org.mariella.persistence.query;

public class ConditionalInnerJoin extends ConditionJoin {

    @Override
    protected void printJoin(StringBuilder b) {
        b.append(" INNER JOIN ");
    }

}
