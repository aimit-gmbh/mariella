package org.mariella.persistence.mapping;

import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.schema.PropertyDescription;
import org.mariella.persistence.util.InitializationHelper;

import java.util.*;


public abstract class AbstractClassMapping {
    protected final SchemaMapping schemaMapping;
    protected final ClassDescription classDescription;

    protected final Map<PropertyDescription, PropertyMapping> propertyMappings =
            new HashMap<>();
    protected final List<PhysicalPropertyMapping> physicalPropertyMappingList = new ArrayList<>();
    protected final List<ColumnMapping> persistorGeneratedColumnMappings = new ArrayList<>();

    public AbstractClassMapping(SchemaMapping schemaMapping, ClassDescription classDescription) {
        super();
        this.schemaMapping = schemaMapping;
        this.classDescription = classDescription;
    }

    public void initialize(InitializationHelper<ClassMapping> context) {
        if (getSuperClassMapping() != null) {
            context.ensureInitialized(getSuperClassMapping());
            propertyMappings.putAll(getSuperClassMapping().propertyMappings);
            physicalPropertyMappingList.addAll(0, getSuperClassMapping().physicalPropertyMappingList);
        }
        for (PhysicalPropertyMapping propertyMapping : physicalPropertyMappingList) {
            if (propertyMapping instanceof ColumnMapping columnMapping) {
                if (columnMapping.getValueGenerator() != null && !columnMapping.getValueGenerator().isGeneratedByDatabase()) {
                    persistorGeneratedColumnMappings.add(columnMapping);
                }
            }
        }
    }

    public void postInitialize(InitializationHelper<ClassMapping> context) {
    }

    public SchemaMapping getSchemaMapping() {
        return schemaMapping;
    }

    public ClassDescription getClassDescription() {
        return classDescription;
    }

    public ClassMapping getSuperClassMapping() {
        if (getClassDescription().getSuperClassDescription() == null) {
            return null;
        } else {
            return getSchemaMapping().getClassMapping(getClassDescription().getSuperClassDescription().getClassName());
        }
    }

    protected List<PhysicalPropertyMapping> getPhysicalPropertyMappingList() {
        return physicalPropertyMappingList;
    }

    public List<ColumnMapping> getPersistorGeneratedColumnMappings() {
        return persistorGeneratedColumnMappings;
    }

    public PropertyMapping getPropertyMapping(PropertyDescription propertyDescription) {
        return propertyMappings.get(propertyDescription);
    }

    public PropertyMapping getPropertyMapping(String propertyName) {
        return getPropertyMapping(getClassDescription().getPropertyDescription(propertyName));
    }

    public Collection<PropertyMapping> getPropertyMappings() {
        return propertyMappings.values();
    }

    public void setPropertyMapping(PropertyDescription propertyDescription, PropertyMapping propertyMapping) {
        propertyMappings.put(propertyDescription, propertyMapping);
        if (propertyMapping instanceof PhysicalPropertyMapping ppm) {
            physicalPropertyMappingList.add(ppm);
        }
    }

}
