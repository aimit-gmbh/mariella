package org.mariella.persistence.query;

public class InnerJoin extends Join {

    @Override
    protected void printJoin(StringBuilder b) {
        b.append(", ");
    }

}
