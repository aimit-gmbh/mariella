package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.mapping.ManyToManyAttributeInfo;
import org.mariella.persistence.mapping.ToManyPropertyMapping;

public class ManyToManyAttributeMappingBuilder extends ToManyAttributeMappingBuilder<ManyToManyAttributeInfo> {

    public ManyToManyAttributeMappingBuilder(EntityMappingBuilder entityMappingBuilder, ManyToManyAttributeInfo attributeInfo) {
        super(entityMappingBuilder, attributeInfo);
    }

    @Override
    protected void createBiDirectionalMappingWithoutJoinTable() {
        ToManyPropertyMapping pm = new ToManyPropertyMapping(getClassMapping(), getPropertyDescription());
        getClassMapping().setPropertyMapping(getPropertyDescription(), pm);
        propertyMapping = pm;
    }

    @Override
    protected void createUniDirectionalMappingWithoutJoinTable() {
        throw new IllegalStateException("Unidirectional n:m mappings without join table are not supported!");
    }

}
