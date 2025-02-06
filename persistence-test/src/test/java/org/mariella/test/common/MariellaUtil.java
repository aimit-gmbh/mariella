package org.mariella.test.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.mariella.persistence.database.StandardUUIDConverter;
import org.mariella.persistence.jdbc.JdbcClusterLoader;
import org.mariella.persistence.jdbc.JdbcPersistor;
import org.mariella.persistence.jdbc.JdbcPreparedPersistorStatement;
import org.mariella.persistence.loader.ClusterLoaderConditionProvider;
import org.mariella.persistence.loader.ClusterLoaderConditionProviderImpl;
import org.mariella.persistence.loader.LoaderContext;
import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.mapping.ColumnMapping;
import org.mariella.persistence.mapping.RelationshipPropertyMapping;
import org.mariella.persistence.persistor.ClusterDescription;
import org.mariella.persistence.persistor.ConnectionDatabaseAccess;
import org.mariella.persistence.persistor.DatabaseAccess;
import org.mariella.persistence.persistor.PersistorStrategy;
import org.mariella.persistence.persistor.SimplePersistorStrategy;
import org.mariella.persistence.query.BinaryCondition;
import org.mariella.persistence.query.Expression;
import org.mariella.persistence.query.InCondition;
import org.mariella.persistence.query.JoinBuilder;
import org.mariella.persistence.query.QueryBuilder;
import org.mariella.persistence.query.TableReference;
import org.mariella.persistence.runtime.ModificationTracker;
import org.mariella.persistence.runtime.ModificationTrackerImpl;
import org.mariella.persistence.runtime.PersistenceException;
import org.mariella.persistence.runtime.RIListener;
import org.mariella.persistence.schema.ClassDescription;

public class MariellaUtil {
	public static class InClusterLoaderConditionProvider extends ClusterLoaderConditionProviderImpl {
	    private final String idPropertyPath;
	    private final Collection<UUID> ids;

	    public InClusterLoaderConditionProvider(Collection<UUID> ids) {
	        this(ids, null);
	    }

	    public InClusterLoaderConditionProvider(Collection<UUID> ids, String idPropertyPath) {
	        this.ids = ids;
	        this.idPropertyPath = idPropertyPath != null ? idPropertyPath : "root.id";
	    }

	    @Override
	    public void pathExpressionJoined(QueryBuilder queryBuilder, String pathExpression, ClassMapping classMapping, TableReference tableReference) {
	        List<Expression> inExpressions = new ArrayList<>(ids.size());
	        StandardUUIDConverter guidConverter = StandardUUIDConverter.Singleton;
	        for (Object id : ids) {
	            inExpressions.add(guidConverter.createLiteral(id));
	        }
	        queryBuilder.and(
	                new InCondition(queryBuilder.createColumnReference(idPropertyPath), inExpressions)
	        );
	    }

	    @Override
	    public String[] getConditionPathExpressions() {
	        return new String[]{"root"};
	    }
	}


	private final Mariella mariella;
	private final Connection connection;
	private ModificationTracker modificationTracker;
	
public MariellaUtil(Mariella mariella, Connection connection) {
	this.mariella = mariella;
	this.connection = connection;
	resetModificationTracker();
}

public Mariella getMariella() {
	return mariella;
}

public Connection getConnection() {
	return connection;
}

public ModificationTracker getModificationTracker() {
	return modificationTracker;
}

public void resetModificationTracker() {
	modificationTracker = new ModificationTrackerImpl(mariella.getSchemaMapping().getSchemaDescription());
	modificationTracker.addPersistentListener(new RIListener(modificationTracker));
}

public void persist() throws SQLException {
	persist(new SimplePersistorStrategy<>());
}

public void persist(PersistorStrategy<JdbcPreparedPersistorStatement> strategy) throws SQLException {
	JdbcPersistor persistor = new JdbcPersistor(mariella.getSchemaMapping(), strategy, modificationTracker, getConnection());
	persistor.persist();
}

public long getNextVal(String sequence) {
	try {
		String sql = "select nextval('" + sequence + "')";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()){
				rs.next();
				return rs.getLong(1);
			} 
		}
	} catch(SQLException e) {
		throw new PersistenceException(e);
	}
}

public ClassDescription getClassDescription(Class<?> clazz) {
    return mariella.getSchemaMapping().getSchemaDescription().getClassDescription(clazz.getName());
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
	return load(new ClusterDescription(getClassDescription(entityClass), pathExpressions), isUpdate, new InClusterLoaderConditionProvider(ids));
}

@SuppressWarnings("unchecked")
public <T> List<T> load(final ClusterDescription cd, final boolean isUpdate, final ClusterLoaderConditionProvider conditionProvider) {
	JdbcClusterLoader clusterLoader = new JdbcClusterLoader(mariella.getSchemaMapping(), cd);
	LoaderContext loaderContext = new LoaderContext(modificationTracker);
	loaderContext.setUpdate(isUpdate);
	return (List<T>) clusterLoader.load(new ConnectionDatabaseAccess(getConnection()), loaderContext, conditionProvider);
}

public DatabaseAccess createDatabaseAccess() {
    return new ConnectionDatabaseAccess(connection);
}

}
