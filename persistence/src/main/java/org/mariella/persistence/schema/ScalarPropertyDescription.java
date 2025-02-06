package org.mariella.persistence.schema;

import java.beans.PropertyDescriptor;

public class ScalarPropertyDescription extends PropertyDescription {
	private static final long serialVersionUID = 1L;
	
    public ScalarPropertyDescription(ClassDescription classDescription, PropertyDescriptor propertyDescriptor) {
        super(classDescription, propertyDescriptor);
    }

}
