package org.mariella.persistence.bootstrap;

import jakarta.persistence.PersistenceException;
import org.mariella.persistence.annotations_processing.ClassLoaderPersistenceUnitParser;
import org.mariella.persistence.annotations_processing.PersistenceUnitParser;
import org.mariella.persistence.annotations_processing.UnitInfoBuilder;
import org.mariella.persistence.mapping.UnitInfo;

import javax.sql.DataSource;
import java.util.Map;

public class StandaloneEnvironment extends EnvironmentImpl {

    private ConnectionProvider connectionProvider;

    public StandaloneEnvironment() {
        super();
    }

    @Override
    public void createUnitInfo(String emName, Map<?, ?> properties) {
        try {
            this.properties = properties;
            PersistenceUnitParser parser = createPersistenceUnitParser();
            UnitInfoBuilder builder = new UnitInfoBuilder(parser);
            builder.build();

            for (UnitInfo unitInfo : builder.getUnitInfos()) {
                if (unitInfo.getPersistenceUnitName().equals(emName)) {
                    this.unitInfo = unitInfo;
                }
            }
            if (this.unitInfo == null) {
                throw new IllegalStateException("Could not find any META-INF/persistence.xml having name " + emName);
            }

            persistenceClassResolver = new DefaultClassResolver(getClass().getClassLoader());
        } catch (Throwable t) {
            throw new PersistenceException(t);
        }
    }

    protected PersistenceUnitParser createPersistenceUnitParser() {
        return new ClassLoaderPersistenceUnitParser(getClass().getClassLoader());
    }

    public ConnectionProvider createConnectionProvider() {
        if (connectionProvider == null) {
            connectionProvider = new J2SEConnectionProvider(unitInfo);
        }
        return connectionProvider;
    }

    public void setConnectionProvider(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void setDataSource(DataSource dataSource) {
        setConnectionProvider(new DataSourceConnectionProvider(dataSource));
    }
}
