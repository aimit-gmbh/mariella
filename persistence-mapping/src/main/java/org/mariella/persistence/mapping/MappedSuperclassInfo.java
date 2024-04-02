package org.mariella.persistence.mapping;


public class MappedSuperclassInfo extends MappedClassInfo {

    public String getName() {
        return getClazz().getName();
    }

}
