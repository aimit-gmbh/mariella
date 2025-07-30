package org.mariella.persistence.annotations_processing;

import jakarta.persistence.OneToMany;
import org.mariella.persistence.mapping.RelationAttributeInfo;

public class OneToManyAttributeInfoReferencesResolver extends ToManyAttributeInfoReferencesResolver {

    public OneToManyAttributeInfoReferencesResolver(UnitInfoBuilder unitInfoBuilder, RelationAttributeInfo attrInfo) {
        super(unitInfoBuilder, attrInfo);
    }

    @Override
    String getAnnotatedMappedBy() {
        OneToMany anno = getAnnotatedElement().getAnnotation(OneToMany.class);
        return anno.mappedBy();
    }

    @Override
    Class<?> getAnnotatedTargetEntity() {
        OneToMany anno = getAnnotatedElement().getAnnotation(OneToMany.class);
        return anno.targetEntity();
    }

}
