package org.mariella.persistence.schema;

import java.beans.PropertyDescriptor;

public class ReferencePropertyDescription extends RelationshipPropertyDescription {
	private static final long serialVersionUID = 1L;
	
    public ReferencePropertyDescription(ClassDescription classDescription, PropertyDescriptor propertyDescriptor,
                                        String reversePropertyName) {
        super(classDescription, propertyDescriptor, reversePropertyName);
    }

    public ReferencePropertyDescription(ClassDescription classDescription, PropertyDescriptor propertyDescriptor) {
        super(classDescription, propertyDescriptor);
    }

    public ClassDescription getReferencedClassDescription() {
        return getClassDescription().getSchemaDescription()
                .getClassDescription(getPropertyDescriptor().getPropertyType().getName());
    }

}
