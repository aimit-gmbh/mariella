package org.mariella.persistence.loader;

import org.mariella.persistence.mapping.RelationshipPropertyMapping;
import org.mariella.persistence.query.JoinBuilder;
import org.mariella.persistence.query.QueryBuilder;


public abstract class ClusterLoaderConditionProviderImpl implements ClusterLoaderConditionProvider {

    @Override
    public void aboutToJoinRelationship(QueryBuilder queryBuilder, String pathExpression, RelationshipPropertyMapping rpm,
                                        JoinBuilder joinBuilder) {
    }

}
