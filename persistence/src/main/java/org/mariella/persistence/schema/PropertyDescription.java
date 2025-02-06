package org.mariella.persistence.schema;

import org.mariella.persistence.runtime.BeanInfo;

import java.beans.PropertyDescriptor;
import java.io.Serializable;

public abstract class PropertyDescription implements Serializable {
	private static final long serialVersionUID = 1L;
	
    private final ClassDescription classDescription;
    private final String propertyName;
    private transient PropertyDescriptor propertyDescriptor;

    public PropertyDescription(ClassDescription classDescription, PropertyDescriptor propertyDescriptor) {
        this.classDescription = classDescription;
        this.propertyDescriptor = propertyDescriptor;
        propertyName = propertyDescriptor.getName();
    }

    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    public ClassDescription getClassDescription() {
        return classDescription;
    }

    public void afterDeserialization(BeanInfo beanInfo) {
        propertyDescriptor = beanInfo.getPropertyDescriptor(propertyName);
    }

    public String toString() {
        return classDescription.toString() + "." + getPropertyDescriptor().getName();
    }
}
