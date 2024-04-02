package org.mariella.persistence.generic;

import org.mariella.persistence.mapping.UnitInfo;
import org.mariella.persistence.mapping_builder.DatabaseInfoProvider;
import org.mariella.persistence.mapping_builder.PersistenceBuilder;
import org.mariella.persistence.mapping_builder.PersistenceInfo;

public class GenericPersistenceBuilder extends PersistenceBuilder {

    public GenericPersistenceBuilder(UnitInfo unitInfo, DatabaseInfoProvider databaseInfoProvider) {
        super(unitInfo, databaseInfoProvider);
    }

    @Override
    protected PersistenceInfo createPersistenceInfo() {
        return new PersistenceInfo(new GenericSchema());
    }

}
