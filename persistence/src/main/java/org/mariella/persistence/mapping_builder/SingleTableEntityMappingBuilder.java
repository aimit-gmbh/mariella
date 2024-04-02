package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.StringConverter;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.DiscriminatorColumnInfo;
import org.mariella.persistence.mapping.EntityInfo;
import org.mariella.persistence.mapping.SingleTableClassMapping;

public class SingleTableEntityMappingBuilder extends SelectableHierarchyEntityMappingBuilder {

    public SingleTableEntityMappingBuilder(PersistenceBuilder persistenceBuilder, EntityInfo entityInfo) {
        super(persistenceBuilder, entityInfo);
    }

    @Override
    protected void primitiveBuildMapping() {
        super.primitiveBuildMapping();

        classMapping = new SingleTableClassMapping(persistenceBuilder.getPersistenceInfo().getSchemaMapping(),
                getClassDescription());
        classMapping.setPrimaryTable(table);
        classMapping.setPrimaryUpdateTable(updateTable);
        ((SingleTableClassMapping) classMapping).setDiscriminatorColumn(discriminatorColumn);
        ((SingleTableClassMapping) classMapping).setDiscriminatorValue(discriminatorValue);
    }

    @Override
    protected Column getColumn(DiscriminatorColumnInfo discriminatorColumnInfo) {
        Table table = persistenceBuilder.getTable(getTableInfo());
        return getPersistenceBuilder().getColumn(table, discriminatorColumnInfo.getName(), StringConverter.Singleton);
    }

    @Override
    protected Column getUpdateColumn(DiscriminatorColumnInfo discriminatorColumnInfo) {
        if (updateTable != null) {
            if (persistenceBuilder.hasColumn(updateTable, discriminatorColumnInfo.getName())) {
                return getPersistenceBuilder().getColumn(updateTable, discriminatorColumnInfo.getName(),
                        StringConverter.Singleton);
            }
        }
        return null;
    }

}
