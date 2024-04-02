package org.mariella.persistence.annotations_processing;

import org.mariella.persistence.mapping.EntityInfo;
import org.mariella.persistence.mapping.RelationAttributeInfo;

import java.lang.reflect.AnnotatedElement;

public abstract class RelationAttributeInfoReferencesResolver {

    final UnitInfoBuilder unitInfoBuilder;
    final RelationAttributeInfo relationAttributeInfo;

    public RelationAttributeInfoReferencesResolver(UnitInfoBuilder unitInfoBuilder, RelationAttributeInfo attrInfo) {
        this.unitInfoBuilder = unitInfoBuilder;
        this.relationAttributeInfo = attrInfo;
    }

    public void resolveReferences() {
        Class<?> targetEntity = getAnnotatedTargetEntity();
        if (targetEntity == null || targetEntity == void.class)
            targetEntity = readTargetEntityByReflection();
        relationAttributeInfo.setRelatedEntityInfo(
                (EntityInfo) relationAttributeInfo.getParentClassInfo().getUnitInfo().getClassToInfoMap()
                        .get(targetEntity.getName()));
        if (relationAttributeInfo.getRelatedEntityInfo() == null)
            throw new IllegalStateException(
                    "Could not determine relatedEntityInfo for relation attribute " + relationAttributeInfo.getParentClassInfo()
                            .getName() + "." + relationAttributeInfo.getName());
        String mappedBy = getAnnotatedMappedBy();
        if (mappedBy != null && mappedBy.length() > 0) {
            try {
                relationAttributeInfo.setReverseAttributeInfo(
                        (RelationAttributeInfo) relationAttributeInfo.getRelatedEntityInfo().getAttributeInfo(mappedBy));
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Entity " + relationAttributeInfo.getParentClassInfo()
                        .getName() + " has invalid mappedBy value " + mappedBy + " for attribute "
                        + relationAttributeInfo);
            }
            relationAttributeInfo.getReverseAttributeInfo().setReverseAttributeInfo(relationAttributeInfo);
        }

    }

    abstract Class<?> readTargetEntityByReflection();

    abstract Class<?> getAnnotatedTargetEntity();

    abstract String getAnnotatedMappedBy();

    AnnotatedElement getAnnotatedElement() {
        return unitInfoBuilder.attributeInfoToAnnotatedElementMap.get(relationAttributeInfo);
    }

}
