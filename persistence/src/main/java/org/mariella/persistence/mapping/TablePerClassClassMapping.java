package org.mariella.persistence.mapping;

import org.mariella.persistence.query.SelectItem;
import org.mariella.persistence.query.StringLiteral;
import org.mariella.persistence.query.SubSelectBuilder;
import org.mariella.persistence.query.TableReference;
import org.mariella.persistence.schema.ClassDescription;

import java.util.List;

public class TablePerClassClassMapping extends ClassMapping {

    public TablePerClassClassMapping(SchemaMapping schemaMapping, ClassDescription classDescription) {
        super(schemaMapping, classDescription);
    }

    public SelectItem addDiscriminatorColumn(SubSelectBuilder subSelectBuilder, TableReference tableReference) {
        return subSelectBuilder.addSelectItem(new StringLiteral((String) getDiscriminatorValue()));
    }

    @Override
    public Object getDiscriminatorValue() {
        return getClassDescription().getClassName();
    }

    @Override
    public void registerDiscriminator(HierarchySubSelect hierarchySubSelect) {
        hierarchySubSelect.registerAnonymousDiscriminator();
    }

    @Override
    protected void collectJoinBuilderSubSelects(HierarchySubSelect hierarchySubSelect,
                                                List<PropertyMapping> propertyMappingList) {
        createJoinBuilderSubSelect(hierarchySubSelect, propertyMappingList);

        for (ClassMapping child : getImmediateChildren()) {
            child.collectJoinBuilderSubSelects(hierarchySubSelect, propertyMappingList);
        }
    }

    @Override
    protected boolean maySelectForHierarchy(PropertyMapping propertyMapping) {
        return getPhysicalPropertyMappingList().contains(propertyMapping);
    }

}
