package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Schema;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.schema.SchemaDescription;
import org.mariella.persistence.util.InitializationHelper;

import java.util.*;


public class SchemaMapping {
    protected final SchemaDescription schemaDescription;
    protected final Schema schema;
    private final Map<String, ClassMapping> classMappingMap = new HashMap<>();

    public SchemaMapping(SchemaDescription schemaDescription, Schema schema) {
        super();
        this.schemaDescription = schemaDescription;
        this.schema = schema;
    }

    public SchemaDescription getSchemaDescription() {
        return schemaDescription;
    }

    public Schema getSchema() {
        return schema;
    }

    public void initialize() {
        InitializationHelper<ClassMapping> context = new InitializationHelper<>(ClassMapping::initialize);
        for (ClassMapping classMapping : getClassMappings()) {
            context.ensureInitialized(classMapping);
        }
        postInitialize();
    }

    private void postInitialize() {
        InitializationHelper<ClassMapping> context = new InitializationHelper<>(ClassMapping::postInitialize);
        for (ClassMapping classMapping : getClassMappings()) {
            context.ensureInitialized(classMapping);
        }
    }

    public ClassMapping getClassMapping(String className) {
        return classMappingMap.get(className);
    }

    public void setClassMapping(String className, ClassMapping tableMapping) {
        classMappingMap.put(className, tableMapping);
    }

    public Collection<ClassMapping> getClassMappings() {
        return classMappingMap.values();
    }

    public Collection<Table> getUsedTables() {
        Set<Table> used = new HashSet<>();
        for (ClassMapping cm : getClassMappings()) {
            cm.collectUsedTables(used);
        }
        return used;
    }

    public Collection<Column> getUsedColumns() {
        Set<Column> used = new HashSet<>();
        for (ClassMapping cm : getClassMappings()) {
            cm.collectUsedColumns(used);
        }
        return used;
    }

    public Collection<Table> getUnusedTables() {
        Collection<Table> unused = new HashSet<>(schema.getTables());
        unused.removeAll(getUsedTables());
        return unused;
    }

    public Collection<Column> getUnusedColumns() {
        Collection<Column> unused = new HashSet<>();
        for (Table table : getUsedTables()) {
            unused.addAll(table.getColumns());
        }
        unused.removeAll(getUsedColumns());
        return unused;
    }

}
