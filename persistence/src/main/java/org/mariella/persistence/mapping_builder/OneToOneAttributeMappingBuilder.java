package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.mapping.OneToOneAttributeInfo;

public class OneToOneAttributeMappingBuilder extends ToOneAttributeMappingBuilder<OneToOneAttributeInfo> {

    public OneToOneAttributeMappingBuilder(EntityMappingBuilder entityMappingBuilder, OneToOneAttributeInfo attributeInfo) {
        super(entityMappingBuilder, attributeInfo);
    }

}
