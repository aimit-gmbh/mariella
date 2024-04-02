package org.mariella.persistence.mapping;

import org.mariella.persistence.database.ResultSetReader;
import org.mariella.persistence.query.SubSelectBuilder;
import org.mariella.persistence.query.TableReference;
import org.mariella.persistence.schema.ClassDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimaryKey {
    private final ClassMapping classMapping;
    private final ColumnMapping[] columnMappings;

    private final ColumnMapping[] generatedByDatabaseColumnMappings;
    private final String[] generatedByDatabaseColumnNames;

    public PrimaryKey(ClassMapping classMapping, ColumnMapping[] columnMappings) {
        super();
        this.classMapping = classMapping;
        this.columnMappings = columnMappings;

        List<ColumnMapping> dbGeneratedColumns = new ArrayList<>();
        List<String> dbGeneratedColumnNames = new ArrayList<>();

        for (ColumnMapping columnMapping : columnMappings) {
            if (columnMapping.getValueGenerator() != null) {
                if (columnMapping.getValueGenerator().isGeneratedByDatabase()) {
                    dbGeneratedColumns.add(columnMapping);
                    dbGeneratedColumnNames.add(columnMapping.getUpdateColumn().name());
                }
            }
        }
        generatedByDatabaseColumnMappings = dbGeneratedColumns.toArray(new ColumnMapping[0]);
        generatedByDatabaseColumnNames = dbGeneratedColumnNames.toArray(new String[0]);
    }

    public ColumnMapping[] getColumnMappings() {
        return columnMappings;
    }

    public ColumnMapping[] getGeneratedByDatabaseColumnMappings() {
        return generatedByDatabaseColumnMappings;
    }

    public String[] getGeneratedByDatabaseColumnNames() {
        return generatedByDatabaseColumnNames;
    }

    public void addColumns(SubSelectBuilder subSelectBuilder, TableReference tableReference) {
        for (ColumnMapping columnMapping : columnMappings) {
            columnMapping.addColumns(subSelectBuilder, tableReference);
        }
    }

    public int getIndex(List<PhysicalPropertyMapping> physicalPropertyMappings) {
        return physicalPropertyMappings.indexOf(columnMappings[0]);
    }

    public Object getIdentity(ResultSetReader reader, ObjectFactory factory,
                              ClassDescription classDescription) {
        Map<String, Object> identityMap = new HashMap<>();
        identityMap.put(ClassDescription.TYPE_PROPERTY, classDescription.getClassName());
        if (classMapping.getClassDescription().getIdentityPropertyDescriptions().length == 0) {
            throw new IllegalStateException(
                    "No primary key columns defined for class " + classMapping.getClassDescription().getClassName());
        } else if (classMapping.getClassDescription().getIdentityPropertyDescriptions().length == 1) {
            Object value = columnMappings[0].getObject(reader, factory);
            if (value == null) {
                return null;
            } else {
                identityMap.put(columnMappings[0].getPropertyDescription().getPropertyDescriptor().getName(), value);
            }
        } else if (classMapping.getClassDescription().getIdentityClass() != null) {
            throw new UnsupportedOperationException("Identity class is not supported!");
        } else {
            for (ColumnMapping columnMapping : columnMappings) {
                Object value = columnMapping.getObject(reader, factory);
                if (value == null) {
                    return null;
                } else {
                    identityMap.put(columnMapping.getPropertyDescription().getPropertyDescriptor().getName(), value);
                }
            }
        }
        return identityMap;
    }

    public boolean contains(PropertyMapping pm) {
        for (ColumnMapping cm : columnMappings) {
            if (cm == pm) {
                return true;
            }
        }
        return false;
    }

}
