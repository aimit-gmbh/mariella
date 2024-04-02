package org.mariella.persistence.query;

import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.mapping.RelationshipPropertyMapping;

public class QueryBuilderListenerImpl implements QueryBuilderListener {

    @Override
    public void aboutToJoinRelationship(QueryBuilder queryBuilder, String pathExpression, RelationshipPropertyMapping rpm,
                                        JoinBuilder joinBuilder) {
    }

    @Override
    public void pathExpressionJoined(QueryBuilder queryBuilder, String pathExpression, ClassMapping classMapping,
                                     TableReference tableReference) {
    }

}
