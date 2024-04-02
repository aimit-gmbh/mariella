package org.mariella.persistence.schema;

import java.beans.PropertyDescriptor;

public class CollectionPropertyDescription extends RelationshipPropertyDescription {
    private final String referencedClassName;

    public CollectionPropertyDescription(ClassDescription classDescription, PropertyDescriptor propertyDescriptor,
                                         String referencedClassName, String reversePropertyName) {
        super(classDescription, propertyDescriptor, reversePropertyName);
        this.referencedClassName = referencedClassName;
    }

    public CollectionPropertyDescription(ClassDescription classDescription, PropertyDescriptor propertyDescriptor,
                                         String referencedClassName) {
        super(classDescription, propertyDescriptor);
        this.referencedClassName = referencedClassName;
    }

    public ClassDescription getReferencedClassDescription() {
        return getClassDescription().getSchemaDescription().getClassDescription(referencedClassName);
    }


}
