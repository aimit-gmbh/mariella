package org.mariella.persistence.annotations_processing;

import org.mariella.persistence.mapping.ReflectionUtil;
import org.mariella.persistence.mapping.RelationAttributeInfo;

public abstract class ToManyAttributeInfoReferencesResolver extends
        RelationAttributeInfoReferencesResolver {

    public ToManyAttributeInfoReferencesResolver(UnitInfoBuilder unitInfoBuilder, RelationAttributeInfo attrInfo) {
        super(unitInfoBuilder, attrInfo);
    }

    @Override
    Class<?> readTargetEntityByReflection() {
        return (Class<?>) ReflectionUtil.readCollectionElementType(ReflectionUtil.readType(getAnnotatedElement()));
    }

}
