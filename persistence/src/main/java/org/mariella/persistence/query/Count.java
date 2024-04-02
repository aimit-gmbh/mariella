package org.mariella.persistence.query;

public class Count extends Function {

    public Count(Expression... parameters) {
        super("COUNT", parameters);
    }

}
