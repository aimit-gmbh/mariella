package org.mariella.persistence.query;

import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.schema.PropertyDescription;

public interface PathExpressionVisitor {

    ClassDescription root(String token);

    void property(ClassDescription classDescription, PropertyDescription propertyDescription);

    void property(ClassDescription classDescription, ClassDescription castTo, PropertyDescription propertyDescription);

    void unknownOrAmbigousProperty(ClassDescription classDescription, String propertyName);

    void unknownType(String className);

    void invalidCast(ClassDescription classDescription, ClassDescription castTo);

    void afterEnd(String token);
}
