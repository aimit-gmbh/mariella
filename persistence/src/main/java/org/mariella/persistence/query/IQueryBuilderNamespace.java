package org.mariella.persistence.query;

public interface IQueryBuilderNamespace {


    TableReference getTableReference(String pathExpression);

    boolean hasTableReference(String pathExpression);

    void register(String pathExpression, TableReference tableReference);
}
