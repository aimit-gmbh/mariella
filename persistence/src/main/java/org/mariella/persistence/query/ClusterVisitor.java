package org.mariella.persistence.query;

import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.schema.CollectionPropertyDescription;
import org.mariella.persistence.schema.RelationshipPropertyDescription;

import java.util.List;

public interface ClusterVisitor {

    void beginPathExpression(String pathExpression);

    void endPathExpression(String pathExpression);

    boolean root(Object entity);

    boolean property(Object owner, ClassDescription ownerClassDescription,
                     RelationshipPropertyDescription propertyDescription, Object value);

    boolean indexedProperty(Object owner, ClassDescription ownerClassDescription,
                            CollectionPropertyDescription propertyDescription, int index, Object value);

    void unkownOrAmbigousProperty(List<String> path, int pathIndex);

    void invalidProperty(List<String> path, int pathIndex);

}
