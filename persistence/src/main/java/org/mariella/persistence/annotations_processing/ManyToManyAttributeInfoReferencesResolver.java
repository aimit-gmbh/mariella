package org.mariella.persistence.annotations_processing;

import org.mariella.persistence.mapping.RelationAttributeInfo;

import javax.persistence.ManyToMany;

public class ManyToManyAttributeInfoReferencesResolver extends
        ToManyAttributeInfoReferencesResolver {

    public ManyToManyAttributeInfoReferencesResolver(UnitInfoBuilder unitInfoBuilder, RelationAttributeInfo attrInfo) {
        super(unitInfoBuilder, attrInfo);
    }

    @Override
    String getAnnotatedMappedBy() {
        ManyToMany anno = getAnnotatedElement().getAnnotation(ManyToMany.class);
        return anno.mappedBy();
    }

    @Override
    Class<?> getAnnotatedTargetEntity() {
        ManyToMany anno = getAnnotatedElement().getAnnotation(ManyToMany.class);
        return anno.targetEntity();
    }

}
