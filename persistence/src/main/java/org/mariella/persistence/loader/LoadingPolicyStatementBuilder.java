package org.mariella.persistence.loader;

import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.mapping.RelationshipPropertyMapping;
import org.mariella.persistence.query.*;
import org.mariella.persistence.query.JoinBuilder.JoinType;

import java.util.HashSet;
import java.util.Set;


public class LoadingPolicyStatementBuilder {

    private final LoadingPolicy loadingPolicy;
    private final ClusterLoaderConditionProvider conditionProvider;
    private final LoaderContext loaderContext;

    public LoadingPolicyStatementBuilder(LoadingPolicy loadingPolicy, LoaderContext loaderContext,
                                         ClusterLoaderConditionProvider conditionProvider) {
        super();
        this.loadingPolicy = loadingPolicy;
        this.conditionProvider = conditionProvider;
        this.loaderContext = loaderContext;
    }

    public String createSelectStatement() {
        NamespaceProvider namespaceProvider = new NamespaceProvider();
        final QueryBuilder queryBuilder = new QueryBuilder(loadingPolicy.getLoader().getSchemaMapping(), namespaceProvider);

        QueryBuilderListener selectItemListener = new QueryBuilderListener() {
            public void pathExpressionJoined(QueryBuilder queryBuilder, String pathExpression, ClassMapping classMapping,
                                             TableReference tableReference) {
                if (pathExpression.length() == loadingPolicy.getPathExpression().length()) {
                    loadingPolicy.addObjectSelectItems(queryBuilder, classMapping, tableReference);
                } else {
                    loadingPolicy.addIdentitySelectItems(queryBuilder, classMapping, tableReference);
                }
            }

            @Override
            public void aboutToJoinRelationship(QueryBuilder queryBuilder, String pathExpression, RelationshipPropertyMapping rpm,
                                                JoinBuilder joinBuilder) {
                joinBuilder.setAddToOrderBy(true);
            }
        };

        queryBuilder.addListener(selectItemListener);

        QueryBuilderListener joinTypeListener = new QueryBuilderListener() {
            @Override
            public void pathExpressionJoined(QueryBuilder queryBuilder, String pathExpression, ClassMapping classMapping,
                                             TableReference tableReference) {
            }

            @Override
            public void aboutToJoinRelationship(QueryBuilder queryBuilder, String pathExpression, RelationshipPropertyMapping rpm,
                                                JoinBuilder joinBuilder) {
                if (!loaderContext.getLoadedRelations().contains(pathExpression)) {
                    joinBuilder.setJoinType(JoinType.leftouter);
                    loaderContext.getLoadedRelations().add(pathExpression);
                }
            }
        };
        queryBuilder.addListener(joinTypeListener);

        queryBuilder.join(loadingPolicy.getLoader().getClusterDescription().getRootDescription(), "root");
        queryBuilder.join(loadingPolicy.getPathExpression());
        queryBuilder.removeListener(selectItemListener);

        namespaceProvider.setMode(NamespaceProvider.Mode.conditions);
        queryBuilder.addListener(new ConditionProviderHandler(conditionProvider));
        for (String conditionPathExpression : conditionProvider.getConditionPathExpressions()) {
            queryBuilder.join(conditionPathExpression);
        }

        StringBuilder b = new StringBuilder();
        queryBuilder.getSubSelect().printSql(b);
        return b.toString();
    }

    public static class NamespaceProvider implements QueryBuilderNamespaceProvider {
        private final IQueryBuilderNamespace nsGlobal = new QueryBuilderNamespace();

        private final IQueryBuilderNamespace nsQuery = new QueryBuilderNamespace();
        private final IQueryBuilderNamespace nsConditions = new QueryBuilderNamespace();
        private Mode mode = Mode.query;

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        @Override
        public IQueryBuilderNamespace getNamespace(String pathExpression) {
            if (pathExpression.indexOf('.') == -1) {
                return nsGlobal;
            } else {
                return mode == Mode.query ? nsQuery : nsConditions;
            }
        }

        public enum Mode {
            query,
            conditions
        }

    }

    private static class ConditionProviderHandler implements QueryBuilderListener {
        private final ClusterLoaderConditionProvider conditionProvider;
        private final Set<TableReference> visitedTableReferences = new HashSet<>();
        private boolean processing = false;

        public ConditionProviderHandler(ClusterLoaderConditionProvider conditionProvider) {
            super();
            this.conditionProvider = conditionProvider;
        }

        @Override
        public void aboutToJoinRelationship(QueryBuilder queryBuilder, String pathExpression, RelationshipPropertyMapping rpm,
                                            JoinBuilder joinBuilder) {
            conditionProvider.aboutToJoinRelationship(queryBuilder, pathExpression, rpm, joinBuilder);
        }

        @Override
        public void pathExpressionJoined(QueryBuilder queryBuilder, String pathExpression, ClassMapping classMapping,
                                         TableReference tableReference) {
            if (!processing && !visitedTableReferences.contains(tableReference)) {
                visitedTableReferences.add(tableReference);
                processing = true;
                conditionProvider.pathExpressionJoined(queryBuilder, pathExpression, classMapping, tableReference);
                processing = false;
            }
        }
    }

}
