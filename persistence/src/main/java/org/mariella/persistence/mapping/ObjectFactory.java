package org.mariella.persistence.mapping;

import org.mariella.persistence.schema.PropertyDescription;

public interface ObjectFactory {

    Object getObject(ClassMapping classMapping, Object identity);

    Object createObject(ClassMapping classMapping, Object identity);

    Object createEmbeddableObject(AbstractClassMapping classMapping);

    Object getValue(Object receiver, PropertyDescription propertyDescription);

    void setValue(Object receiver, PropertyDescription propertyDescription, Object value);

    void updateValue(Object receiver, PropertyDescription propertyDescription, Object value);

}
