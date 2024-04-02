package org.mariella.persistence.mapping;


public class EmbeddableInfo extends MappedClassInfo {

    @Override
    public String getName() {
        return getClazz().getSimpleName();
    }

}
