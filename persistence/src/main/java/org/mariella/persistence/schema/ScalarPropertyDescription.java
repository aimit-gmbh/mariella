package org.mariella.persistence.schema;

import java.beans.PropertyDescriptor;

public class ScalarPropertyDescription extends PropertyDescription {
    public ScalarPropertyDescription(ClassDescription classDescription, PropertyDescriptor propertyDescriptor) {
        super(classDescription, propertyDescriptor);
    }

}
