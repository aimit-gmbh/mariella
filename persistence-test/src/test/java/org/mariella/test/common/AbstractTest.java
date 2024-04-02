package org.mariella.test.common;

import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mariella.persistence.bootstrap.ConnectionProvider;
import org.mariella.persistence.bootstrap.J2SEConnectionProvider;
import org.mariella.persistence.bootstrap.StandaloneEnvironment;
import org.mariella.persistence.database.StandardUUIDConverter;
import org.mariella.persistence.loader.ClusterLoaderConditionProviderImpl;
import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.persistor.ConnectionDatabaseAccess;
import org.mariella.persistence.persistor.DatabaseAccess;
import org.mariella.persistence.query.Expression;
import org.mariella.persistence.query.InCondition;
import org.mariella.persistence.query.QueryBuilder;
import org.mariella.persistence.query.TableReference;
import org.mariella.persistence.runtime.ModificationTracker;
import org.mariella.persistence.runtime.ModificationTrackerImpl;
import org.mariella.persistence.runtime.RIListener;
import org.mariella.persistence.schema.ClassDescription;
import org.mariella.test.model.Person;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class AbstractTest {
    public final static String PERSISTENCE_UNIT_NAME = "sample/h2";

    protected StandaloneEnvironment environment;
    protected ModificationTracker modificationTracker;
    protected Connection connection;

    @BeforeEach
    public void beforeEach() throws Exception {
        environment = new StandaloneEnvironment();
        environment.createUnitInfo(PERSISTENCE_UNIT_NAME, new HashMap<>());

        // TODO should be handled somewhere else?
        String jdbcDriverClassName = environment.getUnitInfo().getProperties()
                .getProperty(ConnectionProvider.JDBC_DRIVER_PROPERTY_NAME);
        DriverManager.registerDriver((java.sql.Driver) Class.forName(jdbcDriverClassName).getDeclaredConstructor().newInstance());

        connection = createConnectionProvider().getConnection();

        RunScript.execute(connection, new InputStreamReader(
                Objects.requireNonNull(AbstractTest.class.getResourceAsStream("/create.sql"))));
        connection.commit();

        environment.createSchemaMapping();
    }

    @AfterEach
    public void afterEach() throws SQLException {
        connection.close();
        connection = null;
        environment = null;
    }

    protected ConnectionProvider createConnectionProvider() {
        return new J2SEConnectionProvider(environment.getUnitInfo());
    }

    protected Person createIngrid() {
        return createPerson("ingrid", "Wieser", "Ingrid");
    }

    protected Person createFlani() {
        return createPerson("flani", "Flandorfer", "Christian");
    }

    protected Person createWolfi() {
        return createPerson("wolfi", "Schwarzenbrunner", "Wolfgang");
    }

    protected Person createTina() {
        return createPerson("dr.", "Sulzenbacher", "Tina");
    }

    protected Person createPerson(String alias, String lastName, String firstName) {
        Person p = new Person();
        p.setId(UUID.randomUUID());
        modificationTracker.addNewParticipant(p);
        p.setAlias(alias);
        p.setFirstName(firstName);
        p.setLastName(lastName);
        return p;
    }

    protected void createModificationTracker() {
        ModificationTrackerImpl m = new ModificationTrackerImpl(environment.getSchemaMapping().getSchemaDescription());
        m.addPersistentListener(new RIListener(m));
        modificationTracker = m;
    }

    public DatabaseAccess createDatabaseAccess() {
        return new ConnectionDatabaseAccess(connection);
    }

    protected ClassDescription getClassDescription(Class<?> clazz) {
        return environment.getSchemaMapping().getSchemaDescription().getClassDescription(clazz.getName());
    }

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
        public void pathExpressionJoined(QueryBuilder queryBuilder, String pathExpression, ClassMapping classMapping,
                                         TableReference tableReference) {
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

}
