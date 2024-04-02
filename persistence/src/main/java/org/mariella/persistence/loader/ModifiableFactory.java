package org.mariella.persistence.loader;

import org.mariella.persistence.schema.ClassDescription;


public interface ModifiableFactory {

    Object createModifiable(ClassDescription classDescription);

    Object createEmbeddable(ClassDescription classDescription);

    Class<?> getClass(ClassDescription classDescription);
}
