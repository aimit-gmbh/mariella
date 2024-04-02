package org.mariella.persistence.annotations_processing;

import org.mariella.persistence.mapping.EntityListenerClassInfo;
import org.mariella.persistence.mapping.MappedClassInfo;
import org.mariella.persistence.mapping.UnitInfo;

import java.util.ArrayList;
import java.util.List;

public class EntityListenerClassInfoBuilder {
    final Class<?> listenerClazz;
    final List<MappedClassInfo> usingMappedClassInfos = new ArrayList<>();
    final UnitInfo unitInfo;

    public EntityListenerClassInfoBuilder(Class<?> listenerClazz, UnitInfo unitInfo) {
        this.listenerClazz = listenerClazz;
        this.unitInfo = unitInfo;
    }

    void build() {
        EntityListenerClassInfo info = new EntityListenerClassInfo();
        info.setClazz(listenerClazz);
        info.setUnitInfo(unitInfo);

        new ClassInfoLifecycleEventInfosBuilder(info).buildLifecycleEventInfos();

        unitInfo.getClassToInfoMap().put(listenerClazz.getName(), info);
        for (MappedClassInfo mappedClassInfo : usingMappedClassInfos) {
            mappedClassInfo.getEntityListenerClassInfos().add(info);
        }
    }

}
