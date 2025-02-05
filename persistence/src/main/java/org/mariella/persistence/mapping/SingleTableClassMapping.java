package org.mariella.persistence.mapping;

import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.util.InitializationHelper;
import org.mariella.persistence.util.Util;

public class SingleTableClassMapping extends SelectableHierarchyClassMapping {

    public SingleTableClassMapping(SchemaMapping schemaMapping, ClassDescription classDescription) {
        super(schemaMapping, classDescription);
    }

    @Override
    public void initialize(InitializationHelper<ClassMapping> initializationHelper) {
        super.initialize(initializationHelper);
        Util.assertTrue(primaryTable != null, "No mapping found for superclass");
        Util.assertTrue(primaryUpdateTable != null, "No mapping found for superclass");
    }

    @Override
    protected boolean shouldBeContainedBy(ClassMapping classMapping) {
        return classMapping instanceof SingleTableClassMapping;
    }

}
