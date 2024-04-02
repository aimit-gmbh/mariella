package org.mariella.persistence.annotations_processing;

import org.mariella.persistence.mapping.*;

public class MappedClassInfoReferencesResolver {

    final UnitInfoBuilder unitInfoBuilder;
    final MappedClassInfo mappedClassInfo;

    MappedClassInfoReferencesResolver(UnitInfoBuilder unitInfoBuilder, MappedClassInfo mappedClassInfo) {
        this.unitInfoBuilder = unitInfoBuilder;
        this.mappedClassInfo = mappedClassInfo;
    }

    public void resolveReferences() {
        for (AttributeInfo attrInfo : mappedClassInfo.getAttributeInfos()) {
            if (attrInfo instanceof RelationAttributeInfo) {
                createRelationAttributeInfoReferencesResolver((RelationAttributeInfo) attrInfo).resolveReferences();
            }
        }
        buildAdoptedEntityListenerClassInfos();
        buildAdoptedLifecycleEventInfos();
    }

    private RelationAttributeInfoReferencesResolver createRelationAttributeInfoReferencesResolver(
            RelationAttributeInfo attrInfo) {
        if (attrInfo instanceof ManyToManyAttributeInfo)
            return new ManyToManyAttributeInfoReferencesResolver(unitInfoBuilder, attrInfo);
        if (attrInfo instanceof OneToManyAttributeInfo)
            return new OneToManyAttributeInfoReferencesResolver(unitInfoBuilder, attrInfo);
        if (attrInfo instanceof ManyToOneAttributeInfo)
            return new ManyToOneAttributeInfoReferencesResolver(unitInfoBuilder, attrInfo);
        if (attrInfo instanceof OneToOneAttributeInfo)
            return new OneToOneAttributeInfoReferencesResolver(unitInfoBuilder, attrInfo);
        return null;
    }

    private void buildAdoptedEntityListenerClassInfos() {
        if (mappedClassInfo.isExcludeSuperclassListeners())
            return;

        MappedClassInfo info = mappedClassInfo.getSuperclassInfo();

        if (info != null) {
            buildAdoptedEntityListenerClassInfos(info);
        }
    }


    private void buildAdoptedEntityListenerClassInfos(MappedClassInfo info) {
        for (EntityListenerClassInfo ci : info.getEntityListenerClassInfos()) {
            mappedClassInfo.getEntityListenerClassInfos().add(ci);
        }
    }

    private void buildAdoptedLifecycleEventInfos() {
        MappedClassInfo info = mappedClassInfo.getSuperclassInfo();
        if (info != null) {
            buildAdoptedLifecycleEventInfos(info);
        }
    }

    private void buildAdoptedLifecycleEventInfos(MappedClassInfo info) {
        for (LifecycleEventInfo lc : info.getLifecycleEventInfos()) {
            if (!mappedClassInfo.containsLifecycleEventInfo(lc))
                mappedClassInfo.getLifecycleEventInfos().add(lc);
        }
    }


}
