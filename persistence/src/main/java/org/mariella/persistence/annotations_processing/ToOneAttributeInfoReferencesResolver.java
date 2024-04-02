package org.mariella.persistence.annotations_processing;

import org.mariella.persistence.mapping.ReflectionUtil;
import org.mariella.persistence.mapping.RelationAttributeInfo;

public abstract class ToOneAttributeInfoReferencesResolver extends
        RelationAttributeInfoReferencesResolver {

    public ToOneAttributeInfoReferencesResolver(UnitInfoBuilder unitInfoBuilder, RelationAttributeInfo attrInfo) {
        super(unitInfoBuilder, attrInfo);
    }

    @Override
    Class<?> readTargetEntityByReflection() {
        return (Class<?>) ReflectionUtil.readType(getAnnotatedElement());
    }


}
