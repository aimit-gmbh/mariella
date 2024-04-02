package org.mariella.persistence.annotations_processing;

import org.mariella.persistence.mapping.MappedClassInfo;

public class MappedClassInfoHierarchyBuilder {

    final MappedClassInfo mappedClassInfo;

    MappedClassInfoHierarchyBuilder(MappedClassInfo mappedClassInfo) {
        this.mappedClassInfo = mappedClassInfo;
    }

    void buildHierarchyInfo() {
        mappedClassInfo.setSuperclassInfo(null);
        Class<?> curSuper = mappedClassInfo.getClazz().getSuperclass();
        while (curSuper != Object.class && mappedClassInfo.getSuperclassInfo() == null) {
            mappedClassInfo.setSuperclassInfo(
                    (MappedClassInfo) mappedClassInfo.getUnitInfo().getClassToInfoMap().get(curSuper.getName()));
            curSuper = curSuper.getSuperclass();
        }
        if (mappedClassInfo.getSuperclassInfo() != null) {
            mappedClassInfo.getSubclassInfos().add(mappedClassInfo);
        }

    }

}
