package org.mariella.persistence.query;

import java.util.HashMap;
import java.util.Map;

public class QueryBuilderNamespace implements IQueryBuilderNamespace {
    private final Map<String, TableReference> pathExpressionToTableReferenceMap = new HashMap<>();

    @Override
    public TableReference getTableReference(String pathExpression) {
        return pathExpressionToTableReferenceMap.get(pathExpression);
    }

    @Override
    public boolean hasTableReference(String pathExpression) {
        return pathExpressionToTableReferenceMap.containsKey(pathExpression);
    }

    @Override
    public void register(String pathExpression, TableReference tableReference) {
        pathExpressionToTableReferenceMap.put(pathExpression, tableReference);
    }

}
