package org.mariella.persistence.bootstrap;

import org.mariella.persistence.database.IndexedParameter;
import org.mariella.persistence.database.JdbcParameter;
import org.mariella.persistence.generic.GenericPersistenceBuilder;
import org.mariella.persistence.mapping.SchemaMapping;
import org.mariella.persistence.mapping.UnitInfo;
import org.mariella.persistence.mapping_builder.DatabaseInfoProvider;
import org.mariella.persistence.mapping_builder.DatabaseMetaDataDatabaseInfoProvider;
import org.mariella.persistence.mapping_builder.PersistenceBuilder;

import java.lang.reflect.Constructor;
import java.sql.DatabaseMetaData;
import java.util.Map;


public abstract class EnvironmentImpl implements Environment {
    protected ClassResolver persistenceClassResolver;
    protected UnitInfo unitInfo;
    protected Map<?, ?> properties;
    private SchemaMapping schemaMapping;


    @Override
    public void createSchemaMapping() {
        try {
            ConnectionProvider connectionProvider = createConnectionProvider();
            try {
                DatabaseMetaData metaData = connectionProvider.getConnection().getMetaData();
                DatabaseMetaDataDatabaseInfoProvider databaseInfoProvider = new DatabaseMetaDataDatabaseInfoProvider(metaData);
                Boolean ignoreSchema = getBooleanProperty(IGNORE_DB_SCHEMA);
                if (ignoreSchema != null) {
                    databaseInfoProvider.setIgnoreSchema(ignoreSchema);
                }
                Boolean usernameAsSchema = getBooleanProperty(USERNAME_AS_DB_SCHEMA);
                if (usernameAsSchema != null) {
                    databaseInfoProvider.setUsernameAsSchema(usernameAsSchema);
                }
                Boolean ignoreCatalog = getBooleanProperty(IGNORE_DB_CATALOG);
                if (ignoreCatalog != null) {
                    databaseInfoProvider.setIgnoreCatalog(ignoreCatalog);
                }
                initializeMapping(databaseInfoProvider);
            } finally {
                connectionProvider.close();
            }

            schemaMapping.getSchemaDescription().setSchemaName(unitInfo.getPersistenceUnitName());

            String parameterStyle = unitInfo.getProperties().getProperty(PARAMETER_STYLE, "jdbc");
            if (parameterStyle.equals(PARAMETER_STYLE_JDBC)) {
                schemaMapping.getSchema().setParameterClass(JdbcParameter.class);
            } else if (parameterStyle.equals(PARAMETER_STYLE_INDEXED)) {
                schemaMapping.getSchema().setParameterClass(IndexedParameter.class);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Unable to create schema mapping", e);
        }
    }

    protected void initializeMapping(DatabaseInfoProvider databaseInfoProvider) {
        String persistenceBuilderClassName = getStringProperty(PERSISTENCE_BUILDER);
        PersistenceBuilder persistenceBuilder;
        if (persistenceBuilderClassName != null) {
            try {
                Class<?> persistenceBuilderClass = persistenceClassResolver.resolveClass(persistenceBuilderClassName);
                Constructor<?> constructor = persistenceBuilderClass.getConstructor(UnitInfo.class,
                        DatabaseInfoProvider.class);
                persistenceBuilder = (PersistenceBuilder) constructor.newInstance(unitInfo, databaseInfoProvider);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        } else {
            persistenceBuilder = createPersistenceBuilder(databaseInfoProvider);
        }

        persistenceBuilder.build();
        schemaMapping = persistenceBuilder.getPersistenceInfo().getSchemaMapping();
    }

    protected PersistenceBuilder createPersistenceBuilder(DatabaseInfoProvider databaseInfoProvider) {
        return new GenericPersistenceBuilder(unitInfo, databaseInfoProvider);
    }

    public UnitInfo getUnitInfo() {
        return unitInfo;
    }

    public ClassResolver getPersistenceClassResolver() {
        return persistenceClassResolver;
    }

    public SchemaMapping getSchemaMapping() {
        return schemaMapping;
    }

    protected Boolean getBooleanProperty(String propertyName) {
        Boolean value = getBooleanProperty(properties, propertyName);
        return value == null ? getBooleanProperty(unitInfo.getProperties(), propertyName) : value;
    }

    protected Boolean getBooleanProperty(Map<?, ?> properties, String propertyName) {
        if (properties.get(propertyName) == null) {
            return null;
        } else if (properties.get(propertyName).equals("true")) {
            return true;
        } else if (properties.get(propertyName).equals("false")) {
            return false;
        } else {
            throw new RuntimeException(
                    "Invalid value for boolean property + '" + unitInfo.getProperties().getProperty(propertyName) + "'.");
        }
    }

    protected String getStringProperty(String propertyName) {
        String value = getStringProperty(properties, propertyName);
        return value == null ? getStringProperty(unitInfo.getProperties(), propertyName) : value;
    }

    protected String getStringProperty(Map<?, ?> properties, String propertyName) {
        return (String) properties.get(propertyName);
    }

    protected Object getProperty(String propertyName) {
        Object value = getProperty(properties, propertyName);
        return value == null ? getProperty(unitInfo.getProperties(), propertyName) : value;
    }

    protected Object getProperty(Map<?, ?> properties, String propertyName) {
        return properties.get(propertyName);
    }

}
