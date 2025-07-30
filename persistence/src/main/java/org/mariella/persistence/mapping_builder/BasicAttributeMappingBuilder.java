package org.mariella.persistence.mapping_builder;

import jakarta.persistence.GenerationType;
import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Converter;
import org.mariella.persistence.database.Sequence;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.*;
import org.mariella.persistence.runtime.Introspector;
import org.mariella.persistence.schema.ScalarPropertyDescription;
import org.mariella.persistence.util.Assert;

import java.beans.PropertyDescriptor;

public class BasicAttributeMappingBuilder extends AttributeMappingBuilder<BasicAttributeInfo> {

    public BasicAttributeMappingBuilder(EntityMappingBuilder entityMappingBuilder, BasicAttributeInfo attributeInfo) {
        super(entityMappingBuilder, attributeInfo);
    }

    @Override
    public ScalarPropertyDescription getPropertyDescription() {
        return (ScalarPropertyDescription) super.getPropertyDescription();
    }

    @Override
    public ColumnMapping getPropertyMapping() {
        return (ColumnMapping) super.getPropertyMapping();
    }

    @Override
    public boolean buildDescription() {
        if (attributeInfo.getColumnInfo() != null) {
            if (getClassDescription().getPropertyDescription(attributeInfo.getName()) != null) {
                throw new IllegalStateException();
            }
            PropertyDescriptor propertyDescriptor = Introspector.Singleton.getBeanInfo(getEntityInfo().getClazz())
                    .getPropertyDescriptor(attributeInfo.getName());
            if (propertyDescriptor == null) {
                throw new IllegalStateException(
                        "No PropertyDescriptor for class " + getClassDescription().getClassName() + " and attribute "
                                + attributeInfo.getName());
            }
            propertyDescription = new ScalarPropertyDescription(getClassDescription(), propertyDescriptor);
            getClassDescription().addPropertyDescription(propertyDescription);
            return true;
        } else {
            LOGGER.trace("No column info for attribute {}.{}", getClassDescription().getClassName(), attributeInfo.getName());
            return false;
        }
    }

    @Override
    public void buildMapping() {
        Column readColumn = getReadColumn();
        Column updateColumn = null;
        if (attributeInfo.getColumnInfo().isInsertable() || attributeInfo.getColumnInfo().isUpdatable()) {
            updateColumn = getUpdateColumn();
        }

        ColumnMapping columnMapping = new ColumnMapping(getClassMapping(), getPropertyDescription(),
                attributeInfo.getColumnInfo().isInsertable(), attributeInfo.getColumnInfo().isUpdatable(), readColumn,
                updateColumn);
        if (attributeInfo.getGeneratedValueInfo() != null) {
            if (attributeInfo.getGeneratedValueInfo().getStrategy() == GenerationType.AUTO) {
                if (attributeInfo.isId()) {
                    columnMapping.setValueGenerator(new AutoGenerator());
                } else {
                    throw new UnsupportedOperationException(
                            "Auto generated columns are only supported for primary keys (" + columnMapping + ")");
                }
            } else if (attributeInfo.getGeneratedValueInfo().getStrategy() == GenerationType.SEQUENCE) {
                if (attributeInfo.getGeneratedValueInfo().getGenerator() == null || attributeInfo.getGeneratedValueInfo()
                        .getGenerator().isEmpty()) {
                    throw new IllegalStateException("No generator specified for " + getClassMapping().getClassDescription()
                            .getClassName() + "." + getPropertyDescription().getPropertyDescriptor().getName() + "!");
                }
                Sequence sequence = entityMappingBuilder.getPersistenceBuilder()
                        .getSequence(getClassMapping().getPrimaryUpdateTable().getQualifiedName(),
                                attributeInfo.getGeneratedValueInfo().getGenerator());
                if (sequence == null) {
                    throw new IllegalStateException("The sequence '" + attributeInfo.getGeneratedValueInfo()
                            .getGenerator() + "' specified in "
                            + getClassMapping().getClassDescription()
                            .getClassName()
                            + "." + getPropertyDescription().getPropertyDescriptor()
                            .getName()
                            + " does not exist!");
                }
                columnMapping.setValueGenerator(new SequenceGenerator(sequence));
            }
        }
        getClassMapping().setPropertyMapping(getPropertyDescription(), columnMapping);
        propertyMapping = columnMapping;
    }

    private Column getReadColumn() {
        TableInfo tableInfo = entityMappingBuilder.getTableInfo(attributeInfo);
        return getColumn(tableInfo);
    }

    private Column getUpdateColumn() {
        TableInfo tableInfo = entityMappingBuilder.getUpdateTableInfo(attributeInfo);
        return getColumn(tableInfo);
    }

    private Column getColumn(TableInfo tableInfo) {
        Table table = entityMappingBuilder.getPersistenceBuilder().getTable(tableInfo);
        DatabaseTableInfo dti = entityMappingBuilder.getPersistenceBuilder().getDatabaseTableInfo(tableInfo);
        DatabaseColumnInfo dci = dti.getColumnInfo(attributeInfo.getColumnInfo().getName());
        Assert.isNotNull(dci,
                "No database column info for column " + tableInfo.getName() + "." + attributeInfo.getColumnInfo().getName());
        Converter<?> converter;
        if (attributeInfo.getConverterName() != null) {
            converter = entityMappingBuilder.getPersistenceBuilder().getConverterRegistry()
                    .getNamedConverter(attributeInfo.getConverterName());
        } else {
            converter = entityMappingBuilder.getPersistenceBuilder().getConverterRegistry()
                    .getConverterForColumn(getPropertyDescription().getPropertyDescriptor().getPropertyType(), dci.getType());
        }
        return entityMappingBuilder.getPersistenceBuilder().getColumn(table, attributeInfo.getColumnInfo().getName(), converter);
    }

}
