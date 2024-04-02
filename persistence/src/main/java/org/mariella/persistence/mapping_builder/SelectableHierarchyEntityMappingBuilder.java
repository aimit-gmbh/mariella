package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.mapping.DiscriminatorColumnInfo;
import org.mariella.persistence.mapping.EntityInfo;

public abstract class SelectableHierarchyEntityMappingBuilder extends EntityMappingBuilder {
    protected Column discriminatorColumn;
    protected Column discriminatorUpdateTableColumn;

    protected String discriminatorValue;

    public SelectableHierarchyEntityMappingBuilder(PersistenceBuilder persistenceBuilder, EntityInfo entityInfo) {
        super(persistenceBuilder, entityInfo);
    }

    @Override
    protected void primitiveBuildMapping() {
        if (entityInfo.getDiscriminatorColumnInfo() != null) {
            discriminatorColumn = getColumn(entityInfo.getDiscriminatorColumnInfo());
            discriminatorUpdateTableColumn = getUpdateColumn(entityInfo.getDiscriminatorColumnInfo());
        }

        if (entityInfo.getDiscriminatorValueInfo() != null) {
            discriminatorValue = entityInfo.getDiscriminatorValueInfo().getValue();
        }
    }

    protected abstract Column getColumn(DiscriminatorColumnInfo discriminatorColumnInfo);

    protected abstract Column getUpdateColumn(DiscriminatorColumnInfo discriminatorColumnInfo);

}
