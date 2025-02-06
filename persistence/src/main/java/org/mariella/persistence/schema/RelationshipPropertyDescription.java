package org.mariella.persistence.schema;

import java.beans.PropertyDescriptor;

public abstract class RelationshipPropertyDescription extends PropertyDescription {
	private static final long serialVersionUID = 1L;
	
    private final String reversePropertyName;

    public RelationshipPropertyDescription(ClassDescription classDescription, PropertyDescriptor propertyDescriptor) {
        super(classDescription, propertyDescriptor);
        reversePropertyName = null;
    }

    public RelationshipPropertyDescription(ClassDescription classDescription, PropertyDescriptor propertyDescriptor,
                                           String reversePropertyName) {
        super(classDescription, propertyDescriptor);
        this.reversePropertyName = reversePropertyName;
    }

    public abstract ClassDescription getReferencedClassDescription();

    public String getReversePropertyName() {
        return reversePropertyName;
    }

    public RelationshipPropertyDescription getReversePropertyDescription() {
        if (reversePropertyName == null) {
            return null;
        } else {
            return (RelationshipPropertyDescription) getReferencedClassDescription().getPropertyDescription(reversePropertyName);
        }
    }

}
