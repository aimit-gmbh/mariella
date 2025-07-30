package org.mariella.persistence.annotations_processing;

import jakarta.persistence.IdClass;
import org.mariella.persistence.mapping.AttributeInfo;
import org.mariella.persistence.mapping.EntityInfo;
import org.mariella.persistence.mapping.MappedClassInfo;
import org.mariella.persistence.mapping.MappedSuperclassInfo;

public class EntityInfoAttributeInfosBuilder extends MappedClassInfoAttributeInfosBuilder {

    public EntityInfoAttributeInfosBuilder(UnitInfoBuilder unitInfoBuilder, MappedClassInfo mappedClassInfo,
                                           IModelToDb translator) {
        super(unitInfoBuilder, mappedClassInfo, translator);
    }

    @Override
    void buildAttributeInfos() throws Exception {
        // TODO Auto-generated method stub
        super.buildAttributeInfos();
        buildAdoptedAttributeInfos();
        IdClass idClass = mappedClassInfo.getClazz().getAnnotation(IdClass.class);
        if (idClass != null)
            ((EntityInfo) mappedClassInfo).setCompositeIdClass(idClass.value());
    }

    private void buildAdoptedAttributeInfos() {
        MappedClassInfo info = mappedClassInfo.getSuperclassInfo();
        while (info instanceof MappedSuperclassInfo) {
            buildAdoptedAttributeInfos(info);
            info = info.getSuperclassInfo();
        }
    }

    private void buildAdoptedAttributeInfos(MappedClassInfo info) {
        for (AttributeInfo attrInfo : info.getAttributeInfos()) {
            AttributeInfo adopted = attrInfo.copyForAdoption();
            unitInfoBuilder.attributeInfoToAnnotatedElementMap.put(adopted,
                    unitInfoBuilder.attributeInfoToAnnotatedElementMap.get(attrInfo));
            adopted.setParentClassInfo(mappedClassInfo);
            mappedClassInfo.addAttributeInfo(adopted);
        }
    }

}
