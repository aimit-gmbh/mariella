package org.mariella.persistence.database;

public class JdbcParameter implements Parameter {

    public JdbcParameter(int ignoredParameterIndex) {
    }

    @Override
    public void print(StringBuilder b) {
        b.append("?");
    }
}
