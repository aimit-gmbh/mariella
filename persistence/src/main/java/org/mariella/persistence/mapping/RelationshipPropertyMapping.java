package org.mariella.persistence.mapping;

import org.mariella.persistence.query.JoinBuilder;
import org.mariella.persistence.query.SubSelectBuilder;
import org.mariella.persistence.query.TableReference;
import org.mariella.persistence.schema.RelationshipPropertyDescription;


public abstract class RelationshipPropertyMapping extends PropertyMapping {

    public RelationshipPropertyMapping(ClassMapping classMapping, RelationshipPropertyDescription propertyDescription) {
        super(classMapping, propertyDescription);
    }

    public RelationshipPropertyDescription getPropertyDescription() {
        return (RelationshipPropertyDescription) super.getPropertyDescription();
    }

    @Override
    public ClassMapping getClassMapping() {
        return (ClassMapping) super.getClassMapping();
    }

    public RelationshipPropertyMapping getReversePropertyMapping() {
        return (RelationshipPropertyMapping) getReferencedClassMapping().getPropertyMapping(
                getPropertyDescription().getReversePropertyDescription());
    }

    public ClassMapping getReferencedClassMapping() {
        return getClassMapping().getSchemaMapping()
                .getClassMapping(getPropertyDescription().getReferencedClassDescription().getClassName());
    }

    public abstract JoinBuilder createJoinBuilder(SubSelectBuilder subSelectBuilder, TableReference myTableReference);

    protected abstract JoinBuilder createReverseJoinBuilder(SubSelectBuilder subSelectBuilder,
                                                            TableReference referencedTableReference);

}
