package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.*;
import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.schema.PropertyDescription;
import org.mariella.persistence.schema.RelationshipPropertyDescription;
import org.mariella.persistence.schema.ScalarPropertyDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EntityMappingBuilder {
    protected final PersistenceBuilder persistenceBuilder;
    protected final EntityInfo entityInfo;
    private final Map<AttributeInfo, AttributeMappingBuilder<?>> attributeMappingBuilders =
            new HashMap<>();
    protected ClassDescription classDescription;
    protected ClassDescription superClassDescription;
    protected ClassMapping classMapping;
    protected Table table;
    protected Table updateTable;

    public EntityMappingBuilder(PersistenceBuilder persistenceBuilder, EntityInfo entityInfo) {
        super();
        this.persistenceBuilder = persistenceBuilder;
        this.entityInfo = entityInfo;
    }

    public ClassDescription getClassDescription() {
        return classDescription;
    }

    public ClassDescription getSuperClassDescription() {
        return superClassDescription;
    }

    public ClassMapping getClassMapping() {
        return classMapping;
    }

    public EntityInfo getEntityInfo() {
        return entityInfo;
    }

    public PersistenceBuilder getPersistenceBuilder() {
        return persistenceBuilder;
    }

    public EntityMappingBuilder getSuperEntityMappingBuilder() {
        return entityInfo.getSuperEntityInfo() == null ? null
                : persistenceBuilder.getEntityMappingBuilder(
                entityInfo.getSuperEntityInfo());
    }

    public void buildDescription() {
        classDescription = persistenceBuilder.getPersistenceInfo().getSchemaDescription()
                .getClassDescription(entityInfo.getClazz().getName());
        if (classDescription != null) {
            throw new IllegalStateException();
        }

        if (entityInfo.getSuperEntityInfo() != null) {
            superClassDescription = persistenceBuilder.getPersistenceInfo().getSchemaDescription()
                    .getClassDescription(entityInfo.getSuperclassInfo().getClazz().getName());
        }
        if (superClassDescription == null) {
            classDescription = new ClassDescription(persistenceBuilder.getPersistenceInfo().getSchemaDescription(),
                    entityInfo.getClazz().getName());
        } else {
            classDescription = new ClassDescription(persistenceBuilder.getPersistenceInfo().getSchemaDescription(),
                    superClassDescription, entityInfo.getClazz().getName());
        }
        persistenceBuilder.getPersistenceInfo().getSchemaDescription().addClassDescription(classDescription);

        List<PropertyDescription> identityPropertyDescriptions = new ArrayList<>();
        for (AttributeInfo attributeInfo : entityInfo.getAttributeInfos()) {
            if (attributeInfo instanceof BasicAttributeInfo bai) {
                AttributeMappingBuilder<?> amb = getAttributeMappingBuilder(bai);
                if (amb.buildDescription()) {
                    if (amb.getPropertyDescription() != null && bai.isId()) {
                        identityPropertyDescriptions.add(amb.getPropertyDescription());
                    }
                } else {
                    attributeMappingBuilders.remove(amb.getAttributeInfo());
                }
            }
        }
        if (!identityPropertyDescriptions.isEmpty()) {
            classDescription.setIdentityPropertyDescriptions(
                    identityPropertyDescriptions.toArray(
                            new PropertyDescription[0]));
        }
    }

    public void buildRelationshipDescriptions() {
        for (AttributeInfo attributeInfo : entityInfo.getAttributeInfos()) {
            if (!(attributeInfo instanceof BasicAttributeInfo)) {
                getAttributeMappingBuilder(attributeInfo).buildDescription();
            }
        }
    }

    protected abstract void primitiveBuildMapping();

    public void buildMapping() {
        TableInfo tableInfo = getTableInfo();
        table = persistenceBuilder.getTable(tableInfo);

        TableInfo updateTableInfo = getUpdateTableInfo();
        updateTable = updateTableInfo == null ? table : persistenceBuilder.getTable(updateTableInfo);

        primitiveBuildMapping();

        persistenceBuilder.getPersistenceInfo().getSchemaMapping()
                .setClassMapping(getClassDescription().getClassName(), classMapping);
        buildBasicAttributeMappings();
    }

    protected void buildBasicAttributeMappings() {
        for (PropertyDescription pd : getClassDescription().getPropertyDescriptions()) {
            if (pd instanceof ScalarPropertyDescription && !getClassDescription().isInherited(pd)) {
                BasicAttributeInfo attributeInfo = (BasicAttributeInfo) getAttributeInfo(pd.getPropertyDescriptor().getName());
                getAttributeMappingBuilder(attributeInfo).buildMapping();
            }
        }
    }

    public void buildRelationAttributeMappings() {
        for (PropertyDescription pd : getClassDescription().getPropertyDescriptions()) {
            if (pd instanceof RelationshipPropertyDescription && !getClassDescription().isInherited(pd)) {
                RelationAttributeInfo attributeInfo = (RelationAttributeInfo) getAttributeInfo(
                        pd.getPropertyDescriptor().getName());
                getAttributeMappingBuilder(attributeInfo).buildMapping();
            }
        }
    }

    protected AttributeMappingBuilder<?> getAttributeMappingBuilder(AttributeInfo attributeInfo) {
        AttributeMappingBuilder<?> amb = attributeMappingBuilders.get(attributeInfo);
        if (amb == null) {
            if (attributeInfo instanceof BasicAttributeInfo) {
                amb = new BasicAttributeMappingBuilder(this, (BasicAttributeInfo) attributeInfo);
            } else if (attributeInfo instanceof ManyToManyAttributeInfo) {
                amb = new ManyToManyAttributeMappingBuilder(this, (ManyToManyAttributeInfo) attributeInfo);
            } else if (attributeInfo instanceof OneToManyAttributeInfo) {
                amb = new OneToManyAttributeMappingBuilder(this, (OneToManyAttributeInfo) attributeInfo);
            } else if (attributeInfo instanceof OneToOneAttributeInfo) {
                amb = new OneToOneAttributeMappingBuilder(this, (OneToOneAttributeInfo) attributeInfo);
            } else if (attributeInfo instanceof ManyToOneAttributeInfo) {
                amb = new ManyToOneAttributeMappingBuilder(this, (ManyToOneAttributeInfo) attributeInfo);
            } else {
                throw new IllegalArgumentException();
            }
            attributeMappingBuilders.put(attributeInfo, amb);
        }
        return amb;
    }

    private AttributeInfo getAttributeInfo(String attributeName) {
        return entityInfo.lookupAttributeInfo(attributeName);
    }

    public boolean isInherited(AttributeInfo attributeInfo) {
        return !entityInfo.hasAttributeInfo(attributeInfo.getName());
    }

    protected boolean isTableInfoLocallyDefined() {
        return entityInfo.getTableInfo() != null;
    }

    protected TableInfo getTableInfo() {
        if (entityInfo.getTableInfo() != null) {
            return entityInfo.getTableInfo();
        } else {
            EntityInfo currentEntityInfo = entityInfo.getSuperEntityInfo();
            while (currentEntityInfo != null) {
                if (currentEntityInfo.getTableInfo() != null) {
                    return currentEntityInfo.getTableInfo();
                }
                currentEntityInfo = currentEntityInfo.getSuperEntityInfo();
            }
        }
        return null;
    }

    protected TableInfo getTableInfo(AttributeInfo attributeInfo) {
        return getTableInfo();
    }

    protected TableInfo getUpdateTableInfo(AttributeInfo attributeInfo) {
        return getUpdateTableInfo();
    }

    protected TableInfo getUpdateTableInfo() {
        if (entityInfo.getUpdateTableInfo() != null) {
            return entityInfo.getUpdateTableInfo();
        } else {
            EntityInfo currentEntityInfo = entityInfo.getSuperEntityInfo();
            while (currentEntityInfo != null) {
                if (currentEntityInfo.getUpdateTableInfo() != null) {
                    return currentEntityInfo.getUpdateTableInfo();
                }
                currentEntityInfo = currentEntityInfo.getSuperEntityInfo();
            }
        }
        return getTableInfo();
    }

    public TableInfo getTableInfoForForeignKeyColumn(String columnName) {
        return getTableInfo();
    }

    public TableInfo getUpdateTableInfoForForeignKeyColumn(String columnName) {
        return getUpdateTableInfo();
    }

    public ColumnMapping getColumnMapping(Column column) {
        for (AttributeMappingBuilder<?> attributeMappingBuilder : attributeMappingBuilders.values()) {
            if (attributeMappingBuilder instanceof BasicAttributeMappingBuilder) {
                ColumnMapping cm = ((BasicAttributeMappingBuilder) attributeMappingBuilder).getPropertyMapping();
                if (cm.getReadColumn() == column) {
                    return cm;
                }
            }
        }
        if (getSuperEntityMappingBuilder() != null) {
            return getSuperEntityMappingBuilder().getColumnMapping(column);
        } else {
            return null;
        }
    }

}
