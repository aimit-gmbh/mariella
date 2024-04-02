package org.mariella.persistence.query;

public interface QueryBuilderNamespaceProvider {
    IQueryBuilderNamespace getNamespace(String pathExpression);
}
