package org.mariella.persistence.runtime;

import org.mariella.persistence.schema.SchemaDescription;

public class ModificationTrackerImpl extends AbstractModificationTrackerImpl {
    private final SchemaDescription schemaDescription;

    public ModificationTrackerImpl(SchemaDescription schemaDescription) {
        super();
        this.schemaDescription = schemaDescription;
    }

    @Override
    public SchemaDescription getSchemaDescription() {
        return schemaDescription;
    }
}
