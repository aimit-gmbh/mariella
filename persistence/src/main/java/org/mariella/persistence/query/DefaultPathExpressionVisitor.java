package org.mariella.persistence.query;

import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.schema.PropertyDescription;

public abstract class DefaultPathExpressionVisitor implements PathExpressionVisitor {

    public void afterEnd(String token) {
        throw new IllegalStateException("after end!");
    }

    public void property(ClassDescription classDescription, PropertyDescription propertyDescription) {
    }

    @Override
    public void property(ClassDescription classDescription, ClassDescription castTo, PropertyDescription propertyDescription) {
        throw new IllegalStateException("casted properties are not supported!");
    }

    public abstract ClassDescription root(String token);

    public void unknownOrAmbigousProperty(ClassDescription classDescription, String propertyName) {
        throw new IllegalStateException(
                "unkown or ambigous property: " + propertyName + " for class " + classDescription.getClassName());
    }

    @Override
    public void unknownType(String className) {
        throw new IllegalStateException("unknown type: " + className);
    }

    @Override
    public void invalidCast(ClassDescription classDescription, ClassDescription castTo) {
        throw new IllegalStateException("invalid cast from " + classDescription.getClassName() + " to " + castTo.getClassName());
    }

}
