package org.mariella.persistence.query;

public class Sum extends Function {

    public Sum(Expression... parameters) {
        super("SUM", parameters);
    }

}
