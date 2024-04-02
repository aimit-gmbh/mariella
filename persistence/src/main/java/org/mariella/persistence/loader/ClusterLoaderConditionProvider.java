package org.mariella.persistence.loader;

import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.mapping.RelationshipPropertyMapping;
import org.mariella.persistence.query.JoinBuilder;
import org.mariella.persistence.query.QueryBuilder;
import org.mariella.persistence.query.QueryBuilderListener;
import org.mariella.persistence.query.TableReference;


public interface ClusterLoaderConditionProvider extends QueryBuilderListener {
    ClusterLoaderConditionProvider Default = new ClusterLoaderConditionProvider() {
        @Override
        public void pathExpressionJoined(QueryBuilder queryBuilder, String pathExpression, ClassMapping classMapping,
                                         TableReference tableReference) {
        }

        @Override
        public void aboutToJoinRelationship(QueryBuilder queryBuilder, String pathExpression, RelationshipPropertyMapping rpm,
                                            JoinBuilder joinBuilder) {
        }

        @Override
        public String[] getConditionPathExpressions() {
            return new String[]{};
        }
    };

    String[] getConditionPathExpressions();

}
