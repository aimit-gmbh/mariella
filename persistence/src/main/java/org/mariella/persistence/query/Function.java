package org.mariella.persistence.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Function implements Expression {
    private final String sqlName;
    private final List<Expression> parameters = new ArrayList<>();

    public Function(String sqlName, Expression... parameters) {
        super();
        this.sqlName = sqlName;
        this.parameters.addAll(Arrays.asList(parameters));
    }

    public String getSqlName() {
        return sqlName;
    }

    public List<Expression> getParameters() {
        return parameters;
    }

    @Override
    public void printSql(StringBuilder b) {
        b.append(sqlName);
        b.append('(');
        boolean first = true;
        for (Expression parameter : parameters) {
            if (first)
                first = false;
            else
                b.append(", ");
            parameter.printSql(b);
        }
        b.append(')');
    }

}
