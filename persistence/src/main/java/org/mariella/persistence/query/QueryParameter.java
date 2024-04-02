package org.mariella.persistence.query;

import org.mariella.persistence.database.Parameter;

public class QueryParameter implements Expression {
    private final Parameter parameter;

    public QueryParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public void printSql(StringBuilder b) {
        parameter.print(b);
    }

}
