package org.mariella.persistence.annotations_processing;

import jakarta.persistence.OneToOne;
import org.mariella.persistence.mapping.RelationAttributeInfo;

public class OneToOneAttributeInfoReferencesResolver extends ToOneAttributeInfoReferencesResolver {

    public OneToOneAttributeInfoReferencesResolver(UnitInfoBuilder unitInfoBuilder, RelationAttributeInfo attrInfo) {
        super(unitInfoBuilder, attrInfo);
    }

    @Override
    String getAnnotatedMappedBy() {
        OneToOne anno = getAnnotatedElement().getAnnotation(OneToOne.class);
        return anno.mappedBy();
    }

    @Override
    Class<?> getAnnotatedTargetEntity() {
        OneToOne anno = getAnnotatedElement().getAnnotation(OneToOne.class);
        return anno.targetEntity();
    }

}
