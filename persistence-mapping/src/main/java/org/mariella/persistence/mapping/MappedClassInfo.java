package org.mariella.persistence.mapping;

import java.io.PrintStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public abstract class MappedClassInfo extends ClassInfo {

    private final List<MappedClassInfo> subclassInfos = new ArrayList<>();
    private final Map<String, AttributeInfo> nameToAttributeInfoMap = new HashMap<>();
    private MappedClassInfo superclassInfo;
    private List<AttributeInfo> attributeInfos;
    private InheritanceInfo inheritanceInfo;
    private List<EntityListenerClassInfo> entityListenerClassInfos = new ArrayList<>();
    private String name = null;
    private boolean excludeSuperclassListeners;

    public boolean containsLifecycleEventInfo(LifecycleEventInfo info) {
        for (LifecycleEventInfo each : getLifecycleEventInfos()) {
            if (each.getEventType().equals(info.getEventType()) && each.getMethod().getName().equals(info.getMethod().getName()))
                return true;
        }
        return false;
    }


    Class<?> getClassMemberType(AnnotatedElement ae) {
        if (ae instanceof Field)
            return (Class<?>) ((Field) ae).getGenericType();
        else
            return (Class<?>) ((Method) ae).getGenericReturnType();
    }

    public List<AttributeInfo> getAttributeInfos() {
        return attributeInfos;
    }

    public void setAttributeInfos(List<AttributeInfo> attributeInfos) {
        this.attributeInfos = attributeInfos;
    }

    public boolean hasAttributeInfo(String name) {
        return nameToAttributeInfoMap.containsKey(name);
    }

    public AttributeInfo getAttributeInfo(String name) {
        AttributeInfo result = nameToAttributeInfoMap.get(name);
        if (result == null)
            throw new IllegalArgumentException("No AttributeInfo named " + name + " found in " + getName());
        return result;
    }

    public AttributeInfo lookupAttributeInfo(String name) {
        AttributeInfo attrInfo = nameToAttributeInfoMap.get(name);
        if (attrInfo != null) return attrInfo;
        if (superclassInfo == null)
            return null;
        return superclassInfo.lookupAttributeInfo(name);
    }

    public MappedClassInfo getSuperclassInfo() {
        return superclassInfo;
    }

    public void setSuperclassInfo(MappedClassInfo superclassInfo) {
        this.superclassInfo = superclassInfo;
    }

    public void addAttributeInfo(AttributeInfo attrInfo) {
        attributeInfos.add(attrInfo);

        // keep the original attribute info in the map (see mergeOverriddenAttributes(...)
        if (!nameToAttributeInfoMap.containsKey(attrInfo.getName()))
            nameToAttributeInfoMap.put(attrInfo.getName(), attrInfo);
    }

    public void debugPrint(PrintStream out) {
        out.print(getClazz().getSimpleName());
        if (superclassInfo != null) {
            out.print(" extends ");
            out.print(superclassInfo);
        }
        if (this instanceof MappedSuperclassInfo)
            out.print(" (MappedSuperclass)");
        if (this instanceof EmbeddableInfo)
            out.print(" (Embeddable)");
        out.println();
        for (AttributeInfo ai : attributeInfos)
            ai.debugPrint(out);
        debugPrintLifecycleEventInfos(out);
        if (entityListenerClassInfos.size() > 0) {
            out.print("\tEntityListenerInfos: ");
            for (Iterator<EntityListenerClassInfo> i = entityListenerClassInfos.iterator(); i.hasNext(); ) {
                EntityListenerClassInfo info = i.next();
                out.print(info.getName());
                if (i.hasNext())
                    out.print(", ");
            }
        }
        out.println();
    }

    public String toString() {
        return getClazz() == null ? super.toString() : getClazz().getSimpleName();
    }

    public InheritanceInfo getInheritanceInfo() {
        return inheritanceInfo;
    }

    public void setInheritanceInfo(InheritanceInfo inheritanceInfo) {
        this.inheritanceInfo = inheritanceInfo;
    }

    public List<MappedClassInfo> getSubclassInfos() {
        return subclassInfos;
    }

    public boolean hasSubclassEntities() {
        for (MappedClassInfo sub : getSubclassInfos()) {
            if (sub instanceof EntityInfo)
                return true;
            boolean next = sub.hasSubclassEntities();
            if (next)
                return true;
        }
        return false;
    }

    public List<EntityListenerClassInfo> getEntityListenerClassInfos() {
        return entityListenerClassInfos;
    }

    void setEntityListenerClassInfos(List<EntityListenerClassInfo> entityListenerClassInfos) {
        this.entityListenerClassInfos = entityListenerClassInfos;
    }

    public void mergeOverridenAttributes() {
        for (AttributeInfo attrInfo : nameToAttributeInfoMap.values()) {
            AttributeInfo overriddenAttrInfo = removeOverriddenAttributeInfo(attrInfo);
            if (overriddenAttrInfo != null)
                attrInfo.overrideWith(overriddenAttrInfo);
        }
    }

    AttributeInfo removeOverriddenAttributeInfo(AttributeInfo attrInfo) {
        // look in adopted attribute infos
        for (Iterator<AttributeInfo> it = attributeInfos.iterator(); it.hasNext(); ) {
            AttributeInfo each = it.next();
            if (each == attrInfo) continue;
            if (each.getName().equals(attrInfo.getName())) {
                it.remove();
                return each;
            }
        }
        if (superclassInfo == null) return null;
        return superclassInfo.lookupAttributeInfo(attrInfo.getName());
    }

    public String getName() {
        if (name == null)
            return getClazz().getSimpleName();

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isExcludeSuperclassListeners() {
        return excludeSuperclassListeners;
    }

    public void setExcludeSuperclassListeners(boolean excludeSuperclassListeners) {
        this.excludeSuperclassListeners = excludeSuperclassListeners;
    }

}
