package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.mapping.RelationAttributeInfo;

public abstract class RelationAttributeMappingBuilder<T extends RelationAttributeInfo> extends AttributeMappingBuilder<T> {
    protected ClassMapping referencedClassMapping;

    public RelationAttributeMappingBuilder(EntityMappingBuilder entityMappingBuilder, T attributeInfo) {
        super(entityMappingBuilder, attributeInfo);
    }

    public EntityMappingBuilder getEntityMappingBuilder() {
        return entityMappingBuilder;
    }

    @Override
    public void buildMapping() {
        Class<?> referencedClass = attributeInfo.getRelatedEntityInfo().getClazz();
        referencedClassMapping = getClassMapping().getSchemaMapping().getClassMapping(referencedClass.getName());
    }

}
