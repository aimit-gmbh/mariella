package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.database.Schema;
import org.mariella.persistence.mapping.SchemaMapping;
import org.mariella.persistence.schema.SchemaDescription;

public class PersistenceInfo {
    private final Schema schema;
    private final SchemaMapping schemaMapping;
    private final SchemaDescription schemaDescription;

    public PersistenceInfo(Schema emptySchema) {
        schema = emptySchema;
        schemaDescription = new SchemaDescription();
        schemaMapping = new SchemaMapping(schemaDescription, schema);
    }

    public Schema getSchema() {
        return schema;
    }

    public SchemaMapping getSchemaMapping() {
        return schemaMapping;
    }

    public SchemaDescription getSchemaDescription() {
        return schemaDescription;
    }

}
