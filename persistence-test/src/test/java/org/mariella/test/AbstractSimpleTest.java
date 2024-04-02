package org.mariella.test;

import org.mariella.persistence.jdbc.JdbcClusterLoader;
import org.mariella.persistence.jdbc.JdbcPersistor;
import org.mariella.persistence.jdbc.JdbcPreparedPersistorStatement;
import org.mariella.persistence.loader.ClusterLoaderConditionProvider;
import org.mariella.persistence.loader.LoaderContext;
import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.mapping.ColumnMapping;
import org.mariella.persistence.mapping.RelationshipPropertyMapping;
import org.mariella.persistence.persistor.ClusterDescription;
import org.mariella.persistence.persistor.ConnectionDatabaseAccess;
import org.mariella.persistence.persistor.PersistorStrategy;
import org.mariella.persistence.persistor.SimplePersistorStrategy;
import org.mariella.persistence.query.*;
import org.mariella.test.common.AbstractTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AbstractSimpleTest extends AbstractTest {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractSimpleTest.class);

    protected void persist() throws SQLException {
        persist(new SimplePersistorStrategy<>());
    }

    protected void persist(PersistorStrategy<JdbcPreparedPersistorStatement> strategy) throws SQLException {
        JdbcPersistor persistor = new JdbcPersistor(environment.getSchemaMapping(), strategy, modificationTracker, connection);
        persistor.persist();
        connection.commit();
    }

    @SuppressWarnings("unchecked")
    public <T> T loadById(final ClusterDescription cd, final Object identity, final boolean isUpdate) {
        ClusterLoaderConditionProvider cp = new ClusterLoaderConditionProvider() {
            @Override
            public String[] getConditionPathExpressions() {
                return new String[]{"root"};
            }

            @Override
            public void aboutToJoinRelationship(QueryBuilder queryBuilder, String pathExpression,
                                                RelationshipPropertyMapping rpm,
                                                JoinBuilder joinBuilder) {
            }

            public void pathExpressionJoined(QueryBuilder queryBuilder, String pathExpression, final ClassMapping classMapping,
                                             TableReference tableReference) {
                if (pathExpression.equals("root")) {
                    for (final ColumnMapping columnMapping : classMapping.getPrimaryKey().getColumnMappings()) {
                        Expression condition = BinaryCondition.eq(
                                tableReference.createColumnReference(columnMapping.getReadColumn()),
                                columnMapping.getReadColumn().converter().createLiteral(identity)
                        );
                        queryBuilder.and(condition);
                    }
                }
            }
        };
        List<Object> objects = load(cd, isUpdate, cp);
        return objects != null && !objects.isEmpty() ? (T) objects.get(0) : null;
    }

    public <T> T loadById(Class<T> entityClass, boolean isUpdate, Object id, String... clusterExpressions) {
        if (clusterExpressions == null || clusterExpressions.length == 0) {
            clusterExpressions = new String[]{"root"};
        }
        return loadById(new ClusterDescription(getClassDescription(entityClass), clusterExpressions), id, isUpdate);
    }


    public <T> List<T> loadByIds(Class<T> entityClass, boolean isUpdate, final Collection<UUID> ids, String... pathExpressions) {
        return load(new ClusterDescription(getClassDescription(entityClass), pathExpressions), isUpdate,
                new InClusterLoaderConditionProvider(ids));
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> load(final ClusterDescription cd, final boolean isUpdate,
                               final ClusterLoaderConditionProvider conditionProvider) {
        JdbcClusterLoader clusterLoader = new JdbcClusterLoader(environment.getSchemaMapping(), cd);
        LoaderContext loaderContext = new LoaderContext(modificationTracker);
        loaderContext.setUpdate(isUpdate);
        return (List<T>) clusterLoader.load(new ConnectionDatabaseAccess(connection), loaderContext, conditionProvider);
    }

}
