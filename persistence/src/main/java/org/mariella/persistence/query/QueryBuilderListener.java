package org.mariella.persistence.query;

import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.mapping.RelationshipPropertyMapping;

public interface QueryBuilderListener {
    QueryBuilderListener Default = new QueryBuilderListenerImpl();

    void aboutToJoinRelationship(QueryBuilder queryBuilder, String pathExpression, RelationshipPropertyMapping rpm,
                                 JoinBuilder joinBuilder);

    void pathExpressionJoined(QueryBuilder queryBuilder, String pathExpression, ClassMapping classMapping,
                              TableReference tableReference);

}
