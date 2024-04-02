package org.mariella.persistence.bootstrap;

import org.mariella.persistence.mapping.SchemaMapping;
import org.mariella.persistence.mapping_builder.DatabaseMetaDataDatabaseInfoProvider;

import java.util.Map;

public interface Environment {
    String PERSISTENCE_BUILDER = "org.mariella.persistence.persistenceBuilder";
    String DEFAULT_BATCH_STRATEGY = "org.mariella.persistence.defaultBatchStrategy";
    String PARAMETER_STYLE = "org.mariella.persistence.parameter_style";
    String PARAMETER_STYLE_JDBC = "jdbc";
    String PARAMETER_STYLE_INDEXED = "indexed";
    String IGNORE_DB_SCHEMA = DatabaseMetaDataDatabaseInfoProvider.class.getName()
            + ".ignoreSchema";
    String USERNAME_AS_DB_SCHEMA = DatabaseMetaDataDatabaseInfoProvider.class.getName()
            + ".usernameAsSchema";
    String IGNORE_DB_CATALOG = DatabaseMetaDataDatabaseInfoProvider.class.getName()
            + ".ignoreCatalog";

    SchemaMapping getSchemaMapping();

    ClassResolver getPersistenceClassResolver();

    ConnectionProvider createConnectionProvider();

    void createUnitInfo(String emName, Map<?, ?> properties);

    void createSchemaMapping();
}
