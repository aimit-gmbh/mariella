package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.mapping.AttributeInfo;
import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.mapping.EntityInfo;
import org.mariella.persistence.mapping.PropertyMapping;
import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.schema.PropertyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AttributeMappingBuilder<T extends AttributeInfo> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AttributeMappingBuilder.class);

    protected final EntityMappingBuilder entityMappingBuilder;
    protected final T attributeInfo;
    protected PropertyDescription propertyDescription;
    protected PropertyMapping propertyMapping;

    public AttributeMappingBuilder(EntityMappingBuilder entityMappingBuilder, T attributeInfo) {
        super();
        this.entityMappingBuilder = entityMappingBuilder;
        this.attributeInfo = attributeInfo;
    }


    public T getAttributeInfo() {
        return attributeInfo;
    }

    public EntityInfo getEntityInfo() {
        return entityMappingBuilder.getEntityInfo();
    }

    public ClassDescription getClassDescription() {
        return entityMappingBuilder.getClassDescription();
    }

    public ClassMapping getClassMapping() {
        return entityMappingBuilder.getClassMapping();
    }

    public PropertyDescription getPropertyDescription() {
        return propertyDescription;
    }

    public PropertyMapping getPropertyMapping() {
        return propertyMapping;
    }

    public abstract boolean buildDescription();

    public abstract void buildMapping();


}
