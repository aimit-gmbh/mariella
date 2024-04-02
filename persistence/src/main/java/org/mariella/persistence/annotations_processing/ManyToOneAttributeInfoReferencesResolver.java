package org.mariella.persistence.annotations_processing;

import org.mariella.persistence.mapping.RelationAttributeInfo;

import javax.persistence.ManyToOne;

public class ManyToOneAttributeInfoReferencesResolver extends ToOneAttributeInfoReferencesResolver {

    public ManyToOneAttributeInfoReferencesResolver(UnitInfoBuilder unitInfoBuilder, RelationAttributeInfo attrInfo) {
        super(unitInfoBuilder, attrInfo);
    }

    @Override
    String getAnnotatedMappedBy() {
        return null;
    }

    @Override
    Class<?> getAnnotatedTargetEntity() {
        ManyToOne anno = getAnnotatedElement().getAnnotation(ManyToOne.class);
        return anno.targetEntity();
    }

}
