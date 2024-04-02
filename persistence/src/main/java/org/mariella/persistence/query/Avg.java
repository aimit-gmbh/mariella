package org.mariella.persistence.query;

public class Avg extends Function {

    public Avg(Expression... parameters) {
        super("AVG", parameters);
    }

}
