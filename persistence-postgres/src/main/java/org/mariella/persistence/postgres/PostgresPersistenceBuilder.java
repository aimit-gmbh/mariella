package org.mariella.persistence.postgres;

import org.mariella.persistence.database.StandardUUIDConverter;
import org.mariella.persistence.mapping.UnitInfo;
import org.mariella.persistence.mapping_builder.ConverterRegistryImpl;
import org.mariella.persistence.mapping_builder.DatabaseInfoProvider;
import org.mariella.persistence.mapping_builder.PersistenceBuilder;
import org.mariella.persistence.mapping_builder.PersistenceInfo;

import java.sql.Types;
import java.util.UUID;

public class PostgresPersistenceBuilder extends PersistenceBuilder {

    public PostgresPersistenceBuilder(UnitInfo unitInfo, DatabaseInfoProvider databaseInfoProvider) {
        super(unitInfo, databaseInfoProvider);
        converterRegistry.registerConverterFactory(Types.OTHER, UUID.class,
                new ConverterRegistryImpl.ConverterFactoryImpl(StandardUUIDConverter.Singleton));
    }

    @Override
    protected PersistenceInfo createPersistenceInfo() {
        return new PersistenceInfo(new PostgresSchema());
    }
}
