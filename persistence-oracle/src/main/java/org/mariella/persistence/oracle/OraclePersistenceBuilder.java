package org.mariella.persistence.oracle;

import org.mariella.persistence.mapping.UnitInfo;
import org.mariella.persistence.mapping_builder.ConverterRegistryImpl;
import org.mariella.persistence.mapping_builder.DatabaseInfoProvider;
import org.mariella.persistence.mapping_builder.PersistenceBuilder;
import org.mariella.persistence.mapping_builder.PersistenceInfo;

import java.sql.Types;
import java.util.UUID;

public class OraclePersistenceBuilder extends PersistenceBuilder {

    public OraclePersistenceBuilder(UnitInfo unitInfo, DatabaseInfoProvider databaseInfoProvider) {
        super(unitInfo, databaseInfoProvider);
        converterRegistry.registerConverterFactory(Types.VARBINARY, UUID.class,
                new ConverterRegistryImpl.ConverterFactoryImpl(OracleUUIDConverter.Singleton));
    }

    @Override
    protected PersistenceInfo createPersistenceInfo() {
        return new PersistenceInfo(new OracleSchema());
    }

}
