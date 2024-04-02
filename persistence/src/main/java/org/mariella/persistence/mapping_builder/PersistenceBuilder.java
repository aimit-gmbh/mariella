package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Converter;
import org.mariella.persistence.database.Sequence;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.*;
import org.mariella.persistence.schema.ClassDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.InheritanceType;
import java.util.HashMap;
import java.util.Map;

public abstract class PersistenceBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceBuilder.class);

    protected final UnitInfo unitInfo;
    protected final PersistenceInfo persistenceInfo;
    protected final DatabaseInfoProvider databaseInfoProvider;
    protected final Map<TableInfo, DatabaseTableInfo> tableInfos = new HashMap<>();
    protected final Map<TableInfo, Table> tables = new HashMap<>();
    protected final Map<String, Sequence> sequences = new HashMap<>();
    protected final Map<EntityInfo, EntityMappingBuilder> entityMappingBuilders = new HashMap<>();
    // TODO ms: die sequences pro table brauch ich das für memox v2 fallback szenario, bitte nicht entfernen.
    protected GenericSequenceProvider genericSequenceProvider = null;
    protected ConverterRegistry converterRegistry;

    public PersistenceBuilder(UnitInfo unitInfo, DatabaseInfoProvider databaseInfoProvider) {
        super();
        this.unitInfo = unitInfo;
        persistenceInfo = createPersistenceInfo();
        this.databaseInfoProvider = databaseInfoProvider;

        converterRegistry = new ConverterRegistryImpl();
    }

    protected abstract PersistenceInfo createPersistenceInfo();

    public PersistenceInfo getPersistenceInfo() {
        return persistenceInfo;
    }

    public DatabaseInfoProvider getColumnInfoProvider() {
        return databaseInfoProvider;
    }

    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    public void setConverterRegistry(ConverterRegistry converterRegistry) {
        this.converterRegistry = converterRegistry;
    }

    public UnitInfo getUnitInfo() {
        return unitInfo;
    }

    protected EntityMappingBuilder getEntityMappingBuilder(EntityInfo entityInfo) {
        EntityMappingBuilder emb = entityMappingBuilders.get(entityInfo);
        if (emb == null) {
            if (entityInfo.getInheritanceInfo() != null && entityInfo.getInheritanceInfo()
                    .getStrategy() == InheritanceType.JOINED) {
                emb = new JoinedEntityMappingBuilder(this, entityInfo);
            } else if (entityInfo.getInheritanceInfo() != null && entityInfo.getInheritanceInfo()
                    .getStrategy() == InheritanceType.SINGLE_TABLE) {
                emb = new SingleTableEntityMappingBuilder(this, entityInfo);
            } else if (entityInfo.getInheritanceInfo() == null || entityInfo.getInheritanceInfo()
                    .getStrategy() == InheritanceType.TABLE_PER_CLASS) {
                emb = new TablePerClassEntityMappingBuilder(this, entityInfo);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported inheritance strategy: " + entityInfo.getInheritanceInfo().getStrategy());
            }
            entityMappingBuilders.put(entityInfo, emb);
        }
        return emb;
    }

    protected EntityMappingBuilder getEntityMappingBuilder(ClassMapping classMapping) {
        for (EntityMappingBuilder emb : entityMappingBuilders.values()) {
            if (emb.getClassMapping() == classMapping) {
                return emb;
            }
        }
        return null;
    }

    public void build() {
        for (ClassInfo classInfo : unitInfo.getHierarchyOrderedClassInfos()) {
            if (classInfo instanceof EntityInfo) {
                getEntityMappingBuilder((EntityInfo) classInfo).buildDescription();
            }
        }

        for (ClassInfo classInfo : unitInfo.getHierarchyOrderedClassInfos()) {
            if (classInfo instanceof EntityInfo) {
                getEntityMappingBuilder((EntityInfo) classInfo).buildRelationshipDescriptions();
            }
        }

        for (ClassInfo classInfo : unitInfo.getHierarchyOrderedClassInfos()) {
            if (classInfo instanceof EntityInfo) {
                ClassDescription cd = persistenceInfo.getSchemaDescription().getClassDescription(classInfo.getClazz().getName());
                cd.setAbstract(classInfo.isAbstract());
            }
        }

        persistenceInfo.getSchemaDescription().initialize();

        for (SequenceGeneratorInfo sgi : unitInfo.getSequenceGeneratorInfos()) {
            buildSequence(sgi);
        }

        for (ClassInfo classInfo : unitInfo.getHierarchyOrderedClassInfos()) {
            if (classInfo instanceof EntityInfo) {
                getEntityMappingBuilder((EntityInfo) classInfo).buildMapping();
            }
        }

        for (ClassInfo classInfo : unitInfo.getHierarchyOrderedClassInfos()) {
            if (classInfo instanceof EntityInfo) {
                getEntityMappingBuilder((EntityInfo) classInfo).buildRelationAttributeMappings();
            }
        }
        getPersistenceInfo().getSchemaMapping().initialize();
    }

    protected void buildSequence(SequenceGeneratorInfo sgi) {
        if (sequences.get(sgi.getName()) != null) {
            throw new IllegalStateException(
                    "The sequence with name " + sgi.getName() + " (" + sgi.getSequenceName() + ") has been specified more than " +
                            "once!");
        }
        Sequence sequence = new Sequence();
        sequence.setName(sgi.getSequenceName());
        sequence.setInitialValue(sgi.getInitialValue());
        sequence.setAllocationSize(sgi.getAllocationSize());
        if (persistenceInfo.getSchema().getSequence(sequence.getName()) != null) {
            throw new IllegalStateException(
                    "The sequence with sequence name " + sgi.getName() + " has been specified more than once!");
        }
        persistenceInfo.getSchema().addSequence(sequence);
        sequences.put(sgi.getName(), sequence);
    }

    protected Table getTable(TableInfo tableInfo) {
        Table table = tables.get(tableInfo);
        if (table == null) {
            table = getTable(tableInfo.getCatalog(), tableInfo.getSchema(), tableInfo.getName());
            DatabaseTableInfo dti = databaseInfoProvider.getTableInfo(tableInfo.getCatalog(), tableInfo.getSchema(),
                    tableInfo.getName());
            if (LOGGER.isInfoEnabled())
                LOGGER.info("Loaded info from DB: {}", dti.getName());
            tableInfos.put(tableInfo, dti);
            tables.put(tableInfo, table);
        }
        return table;
    }

    public Table getTable(String catalog, String schema, String name) {
        Table table = persistenceInfo.getSchema().getTable(name);
        if (table == null) {
            DatabaseTableInfo dti = databaseInfoProvider.getTableInfo(catalog, schema, name);
            if (dti == null) {
                throw new RuntimeException("Table " + name + " has not been found!");
            }
            table = new Table(dti.getCatalog(), dti.getSchema(), dti.getName());
            persistenceInfo.getSchema().addTable(table);
        }
        return table;
    }

    protected boolean hasColumn(Table table, String columnName) {
        DatabaseTableInfo dti = databaseInfoProvider.getTableInfo(table.getCatalog(), table.getSchema(), table.getName());
        return dti.getColumnInfo(columnName) != null;
    }

    protected Column getColumn(Table table, String columnName, Converter<?> converter) {
        DatabaseTableInfo dti = databaseInfoProvider.getTableInfo(table.getCatalog(), table.getSchema(), table.getName());
        DatabaseColumnInfo dci = dti.getColumnInfo(columnName);
        if (dci == null) {
            throw new RuntimeException("Column " + table.getName() + "." + columnName + " does not exist!");
        }
        Column column = table.getColumn(dci.getName());
        if (column == null) {
            column = new Column(dci.getName(), dci.getType(), dci.isNullable(), converter);
            if (dti.getPrimaryKey().contains(dci)) {
                table.addPrimaryKeyColumn(column);
            } else {
                table.addColumn(column);
            }
        }
        return column;
    }

    public Sequence getSequence(String primaryTableName, String name) {
        if (genericSequenceProvider != null) {
            // ms: die sequences pro table brauch ich das für memox v2 fallback szenario, bitte nicht entfernen.
            Sequence sequence = genericSequenceProvider.getSequence(primaryTableName, name);
            if (sequence != null)
                return sequence;
        }
        return sequences.get(name);
    }

    public DatabaseTableInfo getDatabaseTableInfo(TableInfo tableInfo) {
        return tableInfos.get(tableInfo);
    }

    public GenericSequenceProvider getGenericSequenceProvider() {
        return genericSequenceProvider;
    }

    public void setGenericSequenceProvider(
            GenericSequenceProvider genericSequenceProvider) {
        this.genericSequenceProvider = genericSequenceProvider;
    }

}
