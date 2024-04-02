package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.mapping.EntityInfo;
import org.mariella.persistence.mapping.TablePerClassClassMapping;

public class TablePerClassEntityMappingBuilder extends EntityMappingBuilder {

    public TablePerClassEntityMappingBuilder(PersistenceBuilder persistenceBuilder, EntityInfo entityInfo) {
        super(persistenceBuilder, entityInfo);
    }

    @Override
    protected void primitiveBuildMapping() {
        classMapping = new TablePerClassClassMapping(persistenceBuilder.getPersistenceInfo().getSchemaMapping(),
                getClassDescription());
        classMapping.setPrimaryTable(table);
        classMapping.setPrimaryUpdateTable(updateTable);
    }

}
