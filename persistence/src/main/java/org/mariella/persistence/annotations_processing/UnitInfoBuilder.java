package org.mariella.persistence.annotations_processing;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import org.mariella.persistence.annotations.Cluster;
import org.mariella.persistence.annotations.DomainDefinition;
import org.mariella.persistence.annotations.DomainDefinitions;
import org.mariella.persistence.annotations.UpdateTable;
import org.mariella.persistence.mapping.*;

import javax.persistence.JoinColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitInfoBuilder {
    final Map<Class<?>, EntityListenerClassInfoBuilder> classToEntityListenerClassInfoBuilder =
            new HashMap<>();
    final Map<AttributeInfo, AnnotatedElement> attributeInfoToAnnotatedElementMap = new HashMap<>();
    private final PersistenceUnitParser persistenceUnitParser;


    public UnitInfoBuilder(PersistenceUnitParser parser) {
        super();
        this.persistenceUnitParser = parser;
    }

    public static IModelToDb getTranslator(UnitInfo unitInfo) {
        return "false".equals(unitInfo.getProperties().getProperty(
                "org.mariella.persistence.db.uppercase")) ? ModelToDbTranslator.LOWERCASE : ModelToDbTranslator.UPPERCASE;
    }

    public void build() {
        try {
            persistenceUnitParser.parsePersistenceUnits();
            for (UnitInfo info : getUnitInfos()) {
                parseAnnotations(info);
            }
            for (UnitInfo info : getUnitInfos()) {
                buildJoinTableInfos(info);
            }
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void buildJoinTableInfos(UnitInfo info) {
        IModelToDb translator = getTranslator(info);
        for (ClassInfo classInfo : info.getClassInfos()) {
            if (classInfo instanceof MappedClassInfo) {
                for (AttributeInfo attributeInfo : ((MappedClassInfo) classInfo).getAttributeInfos()) {
                    if (attributeInfo instanceof ToManyAttributeInfo toMany) {
                        buildJoinTableInfos(toMany, translator);
                    }
                }
            }
        }
    }

    private void buildJoinTableInfos(ToManyAttributeInfo toMany, IModelToDb translator) {
        if (toMany.getJoinTableInfo() != null)
            return;

        AnnotatedElement ae = attributeInfoToAnnotatedElementMap.get(toMany);
        if (ae == null)
            throw new IllegalStateException();

        if (!ae.isAnnotationPresent(javax.persistence.JoinTable.class))
            return;

        javax.persistence.JoinTable joinTable = ae.getAnnotation(javax.persistence.JoinTable.class);

        JoinTableInfo info = new JoinTableInfo();
        info.setCatalog(translator.translate(joinTable.catalog()));
        info.setName(translator.translate(joinTable.name()));
        info.setSchema(translator.translate(joinTable.schema()));
        info.setUniqueConstraintInfos(buildUniqueContraintInfos(joinTable.uniqueConstraints(), translator));
        info.setJoinColumnInfos(buildJoinColumnInfos(joinTable.joinColumns(), translator));
        info.setInverseJoinColumnInfos(buildJoinColumnInfos(joinTable.inverseJoinColumns(), translator));
        toMany.setJoinTableInfo(info);
    }

    private List<JoinColumnInfo> buildJoinColumnInfos(JoinColumn[] joinColumns, IModelToDb translator) {
        List<JoinColumnInfo> infos = new ArrayList<>();
        for (JoinColumn joinCol : joinColumns) {
            infos.add(new JoinColumnInfoBuilder(joinCol, translator).buildJoinColumnInfo());
        }
        return infos;
    }

    private List<UniqueConstraintInfo> buildUniqueContraintInfos(javax.persistence.UniqueConstraint[] uniqueConstraints,
                                                                 IModelToDb translator) {
        List<UniqueConstraintInfo> infos = new ArrayList<>();
        for (UniqueConstraint con : uniqueConstraints) {
            infos.add(new UniqueConstraintInfoBuilder(con, translator).buildUniqueConstraintInfo());
        }
        return infos;
    }

    private void parseAnnotations(UnitInfo unitInfo) throws Exception {
        Map<Class<?>, List<Class<?>>> annotationToClassesMap = readAnnotatedClasses(
                unitInfo,
                Entity.class,
                Embeddable.class,
                Cluster.class,
                SqlResultSetMapping.class,
                SqlResultSetMappings.class,
                NamedNativeQuery.class,
                NamedNativeQueries.class,
                DomainDefinitions.class,
                DomainDefinition.class);
        parseEmbeddables(unitInfo, annotationToClassesMap.get(Embeddable.class));
        parseEntities(unitInfo, annotationToClassesMap.get(Entity.class));
        processEntityListenerClassInfoBuilders();

        for (ClassInfo ci : unitInfo.getHierarchyOrderedClassInfos()) {
            if (ci instanceof MappedClassInfo) {
                new MappedClassInfoReferencesResolver(this, (MappedClassInfo) ci).resolveReferences();
            }
        }

        parseMariellaAnnotations(unitInfo, annotationToClassesMap.get(Cluster.class));
        parseSqlResultSetMappingInstances(unitInfo, annotationToClassesMap.get(SqlResultSetMapping.class));
        parseSqlResultSetMappings(unitInfo, annotationToClassesMap.get(SqlResultSetMappings.class));
        parseNamedNativeQueryInstances(unitInfo, annotationToClassesMap.get(NamedNativeQuery.class));
        parseNamedNativeQueries(unitInfo, annotationToClassesMap.get(NamedNativeQueries.class));
        parseDomainDefinitionInstances(unitInfo, annotationToClassesMap.get(DomainDefinition.class));
        parseDomainDefinitions(unitInfo, annotationToClassesMap.get(DomainDefinitions.class));
    }

    private void parseDomainDefinitions(UnitInfo unitInfo, List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            DomainDefinitions defs = ((AnnotatedElement) clazz).getAnnotation(DomainDefinitions.class);
            for (int i = 0; i < defs.value().length; i++) {
                buildDomainDefinitionInfo(unitInfo, defs.value()[i]);
            }
        }
    }

    private void parseDomainDefinitionInstances(UnitInfo unitInfo, List<Class<?>> resultSetClasses) {
        for (Class<?> clazz : resultSetClasses) {
            DomainDefinition def = ((AnnotatedElement) clazz).getAnnotation(DomainDefinition.class);
            buildDomainDefinitionInfo(unitInfo, def);
        }
    }

    private void buildDomainDefinitionInfo(UnitInfo unitInfo, DomainDefinition def) {
        DomainDefinitionInfo info = new DomainDefinitionInfo();
        info.setLength(def.length());
        info.setName(def.name());
        info.setPrecision(def.precision());
        info.setScale(def.scale());
        info.setSqlType(def.sqlType());
        unitInfo.getDomainDefinitionInfos().add(info);
    }

    private void parseNamedNativeQueries(UnitInfo unitInfo, List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            NamedNativeQueries queries = ((AnnotatedElement) clazz).getAnnotation(NamedNativeQueries.class);
            for (int i = 0; i < queries.value().length; i++) {
                buildNamedNativeQueryInfo(unitInfo, queries.value()[i]);
            }
        }
    }

    private void parseNamedNativeQueryInstances(UnitInfo unitInfo, List<Class<?>> resultSetClasses) {
        for (Class<?> clazz : resultSetClasses) {
            NamedNativeQuery mapping = ((AnnotatedElement) clazz).getAnnotation(NamedNativeQuery.class);
            buildNamedNativeQueryInfo(unitInfo, mapping);
        }
    }

    private void buildNamedNativeQueryInfo(UnitInfo unitInfo, NamedNativeQuery query) {
        NamedNativeQueryInfo info = new NamedNativeQueryInfo();

        QueryHintInfo[] queryHintInfos = new QueryHintInfo[query.hints().length];
        for (int i = 0; i < query.hints().length; i++) {
            queryHintInfos[i] = buildQueryHintInfo(query.hints()[i]);
        }
        info.setName(query.name());
        info.setQuery(query.query());
        info.setQueryHintInfos(queryHintInfos);
        info.setResultClass(query.resultClass());
        info.setSqlResultSetMappingName(query.resultSetMapping());

        unitInfo.getNamedNativeQueryInfos().add(info);
    }

    private QueryHintInfo buildQueryHintInfo(QueryHint queryHint) {
        QueryHintInfo info = new QueryHintInfo();
        info.setName(queryHint.name());
        info.setValue(queryHint.value());
        return info;
    }

    private void parseSqlResultSetMappings(UnitInfo unitInfo, List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            SqlResultSetMappings mappings = ((AnnotatedElement) clazz).getAnnotation(SqlResultSetMappings.class);
            for (int i = 0; i < mappings.value().length; i++) {
                buildSqlResultSetMappingInfo(unitInfo, mappings.value()[i]);
            }
        }
    }

    private void parseSqlResultSetMappingInstances(UnitInfo unitInfo, List<Class<?>> resultSetClasses) {
        for (Class<?> clazz : resultSetClasses) {
            SqlResultSetMapping mapping = ((AnnotatedElement) clazz).getAnnotation(SqlResultSetMapping.class);
            buildSqlResultSetMappingInfo(unitInfo, mapping);
        }
    }

    private void buildSqlResultSetMappingInfo(UnitInfo unitInfo, SqlResultSetMapping mapping) {

        EntityResultInfo[] entityResultInfos = new EntityResultInfo[mapping.entities().length];
        for (int i = 0; i < mapping.entities().length; i++) {
            entityResultInfos[i] = buildEntityResultInfo(mapping.entities()[i]);
        }

        ColumnResultInfo[] columnResultInfos = new ColumnResultInfo[mapping.columns().length];
        for (int i = 0; i < mapping.columns().length; i++) {
            columnResultInfos[i] = buildColumnResultInfo(mapping.columns()[i]);
        }


        SqlResultSetMappingInfo info = new SqlResultSetMappingInfo();
        info.setColumnResultInfos(columnResultInfos);
        info.setEntityResultInfos(entityResultInfos);
        info.setName(mapping.name());
        unitInfo.getSqlResultSetMappingInfos().add(info);
    }

    private ColumnResultInfo buildColumnResultInfo(ColumnResult columnResult) {
        ColumnResultInfo info = new ColumnResultInfo();
        info.setName(columnResult.name());
        return info;
    }

    private EntityResultInfo buildEntityResultInfo(EntityResult entityResult) {
        EntityResultInfo info = new EntityResultInfo();

        FieldResultInfo[] fieldResultInfos = new FieldResultInfo[entityResult.fields().length];
        for (int i = 0; i < entityResult.fields().length; i++) {
            fieldResultInfos[i] = buildFieldResultInfo(entityResult.fields()[i]);
        }
        info.setFieldResultInfos(fieldResultInfos);

        return info;
    }

    private FieldResultInfo buildFieldResultInfo(FieldResult fieldResult) {
        FieldResultInfo info = new FieldResultInfo();
        info.setColumn(fieldResult.column());
        info.setName(fieldResult.name());
        return info;
    }

    private void parseMariellaAnnotations(UnitInfo unitInfo, List<Class<?>> clusterClasses) {
        for (Class<?> clazz : clusterClasses) {
            Cluster cluster = ((AnnotatedElement) clazz).getAnnotation(Cluster.class);
            ClusterInfo clusterInfo = new ClusterInfo();
            clusterInfo.setName(cluster.name());
            clusterInfo.setPathExpressions(cluster.pathExpressions());
            clusterInfo.setClusterClass(clazz);
            Type clusterTypeArg = ReflectionUtil.readTypeArgumentsOfClass(clazz);
            if (!(clusterTypeArg instanceof Class))
                throw new IllegalStateException("Root type of cluster " + clazz + " is not a class " + clusterTypeArg);

            ClassInfo classInfo = unitInfo.getClassToInfoMap().get(((Class<?>) clusterTypeArg).getName());
            if (!(classInfo instanceof EntityInfo))
                throw new IllegalStateException("Root type of cluster " + clazz + " is not an Entity " + clusterTypeArg);
            clusterInfo.setRootEntityName(classInfo.getName());
            unitInfo.getClusterInfos().add(clusterInfo);
        }

    }

    private void parseEmbeddables(UnitInfo unitInfo, List<Class<?>> classes) throws Exception {
        for (Class<?> clazz : classes) {
            parseEmbeddable(unitInfo, clazz);
        }

        IModelToDb translator = getTranslator(unitInfo);
        for (Class<?> clazz : classes) {
            ClassInfo ci = unitInfo.getClassToInfoMap().get(clazz.getName());
            createMappedClassInfoAttributeInfosBuilder(ci, translator).buildAttributeInfos();
        }
    }

    private void parseEmbeddable(UnitInfo unitInfo, Class<?> clazz) {
        EmbeddableInfo info = new EmbeddableInfo();
        info.setClazz(clazz);
        info.setUnitInfo(unitInfo);
        unitInfo.getClassToInfoMap().put(clazz.getName(), info);
    }

    private void parseEntities(UnitInfo unitInfo, List<Class<?>> entityClasses) throws Exception {
        entityClasses = addMappedSuperclasses(entityClasses);
        entityClasses = orderHierarchy(entityClasses);
        buildClassInfos(unitInfo, entityClasses);
    }

    private void processEntityListenerClassInfoBuilders() {
        for (EntityListenerClassInfoBuilder builder : classToEntityListenerClassInfoBuilder.values()) {
            builder.build();
        }
    }

    private void buildClassInfos(UnitInfo unitInfo, List<Class<?>> annotatedClasses) throws Exception {
        IModelToDb translator = getTranslator(unitInfo);
        for (Class<?> clazz : annotatedClasses) {
            buildClassInfo(unitInfo, clazz);
        }
        for (Class<?> clazz : annotatedClasses) {
            ClassInfo ci = unitInfo.getClassToInfoMap().get(clazz.getName());
            if (ci instanceof MappedClassInfo)
                new MappedClassInfoHierarchyBuilder((MappedClassInfo) ci).buildHierarchyInfo();
        }
        for (Class<?> clazz : annotatedClasses) {
            ClassInfo ci = unitInfo.getClassToInfoMap().get(clazz.getName());
            if (ci instanceof MappedClassInfo) {
                createMappedClassInfoAttributeInfosBuilder(ci, translator).buildAttributeInfos();
            }
        }
        for (Class<?> clazz : annotatedClasses) {
            ClassInfo ci = unitInfo.getClassToInfoMap().get(clazz.getName());
            new ClassInfoLifecycleEventInfosBuilder(ci).buildLifecycleEventInfos();
        }
        for (Class<?> clazz : annotatedClasses) {
            ClassInfo ci = unitInfo.getClassToInfoMap().get(clazz.getName());
            if (ci instanceof MappedClassInfo)
                ((MappedClassInfo) ci).mergeOverridenAttributes();
        }
    }

    private MappedClassInfoAttributeInfosBuilder createMappedClassInfoAttributeInfosBuilder(ClassInfo ci, IModelToDb translator) {
        if (ci instanceof EntityInfo)
            return new EntityInfoAttributeInfosBuilder(this, (EntityInfo) ci, translator);

        return new MappedClassInfoAttributeInfosBuilder(this, (MappedClassInfo) ci, translator);
    }

    private void buildClassInfo(UnitInfo unitInfo, Class<?> clazz) {
        MappedClassInfo info;
        if (clazz.isAnnotationPresent(Entity.class)) {
            IModelToDb translator = getTranslator(unitInfo);
            info = new EntityInfo();
            Entity annotation = ((AnnotatedElement) clazz).getAnnotation(Entity.class);
            info.setName("".equals(annotation.name()) ? null : annotation.name());
            info.setExcludeSuperclassListeners(clazz.isAnnotationPresent(ExcludeSuperclassListeners.class));
            if (clazz.isAnnotationPresent(Table.class)) {
                Table table = ((AnnotatedElement) clazz).getAnnotation(Table.class);

                TableTableInfo tti = new TableTableInfo();
                tti.setCatalog(translator.translate(table.catalog()));
                tti.setName(translator.translate(table.name()));
                tti.setSchema(translator.translate(table.schema()));
                ((EntityInfo) info).setTableInfo(tti);
            }
            if (clazz.isAnnotationPresent(UpdateTable.class)) {
                UpdateTable table = ((AnnotatedElement) clazz).getAnnotation(UpdateTable.class);

                UpdateTableInfo uti = new UpdateTableInfo();
                uti.setCatalog(translator.translate(table.catalog()));
                uti.setName(translator.translate(table.name()));
                uti.setSchema(translator.translate(table.schema()));
                ((EntityInfo) info).setUpdateTableInfo(uti);
            }
        } else if (clazz.isAnnotationPresent(MappedSuperclass.class)) {
            info = new MappedSuperclassInfo();
        } else
            throw new RuntimeException("Not a valid class: " + clazz);


        if (clazz.isAnnotationPresent(EntityListeners.class)) {
            EntityListeners entityListeners = ((AnnotatedElement) clazz).getAnnotation(EntityListeners.class);
            for (Class<?> lclazz : entityListeners.value()) {
                buildEntityListenerClassInfoBuilder(unitInfo, lclazz, info);
            }
        }


        info.setUnitInfo(unitInfo);
        info.setClazz(clazz);

        unitInfo.getClassToInfoMap().put(clazz.getName(), info);
        unitInfo.getHierarchyOrderedClassInfos().add(info);

        if (clazz.isAnnotationPresent(Inheritance.class)) {
            info.setInheritanceInfo(new InheritanceInfo());
            info.getInheritanceInfo()
                    .setStrategy(((AnnotatedElement) clazz).getAnnotation(Inheritance.class).strategy());
        }

        if (clazz.isAnnotationPresent(DiscriminatorColumn.class)) {
            if (!(info instanceof EntityInfo))
                throw new IllegalArgumentException(
                        "@DiscriminatorColumn annotation can only be assigned to classes that have an @Entity annotation");
            IModelToDb translator = getTranslator(unitInfo);
            DiscriminatorColumn discrCol = ((AnnotatedElement) clazz).getAnnotation(DiscriminatorColumn.class);
            DiscriminatorColumnInfo discrColumnInfo = new DiscriminatorColumnInfo();
            discrColumnInfo.setColumnDefinition(discrCol.columnDefinition());
            discrColumnInfo.setDiscriminatorType(discrCol.discriminatorType());
            discrColumnInfo.setLength(discrCol.length());
            discrColumnInfo.setName(translator.translate(discrCol.name()));

            ((EntityInfo) info).setDiscriminatorColumnInfo(discrColumnInfo);
        }

        if (clazz.isAnnotationPresent(DiscriminatorValue.class)) {
            if (!(info instanceof EntityInfo))
                throw new IllegalArgumentException(
                        "@DiscriminatorValue annotation can only be assigned to classes that have an @Entity annotation");
            DiscriminatorValue discrValue = ((AnnotatedElement) clazz).getAnnotation(DiscriminatorValue.class);
            DiscriminatorValueInfo discrValueInfo = new DiscriminatorValueInfo();
            discrValueInfo.setValue(discrValue.value());
            ((EntityInfo) info).setDiscriminatorValueInfo(discrValueInfo);
        }

        IModelToDb translator = getTranslator(unitInfo);
        if (clazz.isAnnotationPresent(PrimaryKeyJoinColumns.class)) {
            if (!(info instanceof EntityInfo))
                throw new IllegalArgumentException(
                        "@PrimaryKeyJoinColumns annotation can only be assigned to classes that have an @Entity annotation");
            if (clazz.isAnnotationPresent(PrimaryKeyJoinColumn.class)) {
                throw new IllegalArgumentException(
                        "@PrimaryKeyJoinColumns and @PrimaryKeyJoinColumn annotations must not be used together!");
            }
            PrimaryKeyJoinColumns primaryKeyJoinColumns = ((AnnotatedElement) clazz).getAnnotation(PrimaryKeyJoinColumns.class);
            for (PrimaryKeyJoinColumn primaryKeyJoinColumn : primaryKeyJoinColumns.value()) {
                ((EntityInfo) info).getPrimaryKeyJoinColumnInfos()
                        .add(buildPrimaryKeyJoinColumnInfo(primaryKeyJoinColumn, translator));
            }
        } else if (clazz.isAnnotationPresent(PrimaryKeyJoinColumn.class) && info instanceof EntityInfo) {
            ((EntityInfo) info).getPrimaryKeyJoinColumnInfos()
                    .add(buildPrimaryKeyJoinColumnInfo(((AnnotatedElement) clazz).getAnnotation(PrimaryKeyJoinColumn.class),
                            translator));
        }

        if (clazz.isAnnotationPresent(SequenceGenerator.class)) {
            SequenceGenerator generator = ((AnnotatedElement) clazz).getAnnotation(SequenceGenerator.class);

            SequenceGeneratorInfo sqinfo = new SequenceGeneratorInfo();
            sqinfo.setAllocationSize(generator.allocationSize());
            sqinfo.setInitialValue(generator.initialValue());
            sqinfo.setName(generator.name());
            sqinfo.setSequenceName(generator.sequenceName());
            unitInfo.getSequenceGeneratorInfos().add(sqinfo);
        }

        if (clazz.isAnnotationPresent(TableGenerator.class)) {
            new TableGeneratorInfoBuilder(((AnnotatedElement) clazz).getAnnotation(TableGenerator.class), unitInfo,
                    translator).buildInfo();
        }
    }

    private PrimaryKeyJoinColumnInfo buildPrimaryKeyJoinColumnInfo(PrimaryKeyJoinColumn primaryKeyJoinColumn,
                                                                   IModelToDb translator) {
        PrimaryKeyJoinColumnInfo pkInfo = new PrimaryKeyJoinColumnInfo();
        pkInfo.setColumnDefinition(primaryKeyJoinColumn.columnDefinition());
        pkInfo.setName(translator.translate(primaryKeyJoinColumn.name()));
        pkInfo.setReferencedColumnName(translator.translate(primaryKeyJoinColumn.referencedColumnName()));
        return pkInfo;
    }

    private void buildEntityListenerClassInfoBuilder(UnitInfo unitInfo, Class<?> listenerClazz, MappedClassInfo info) {
        EntityListenerClassInfoBuilder builder = classToEntityListenerClassInfoBuilder.get(listenerClazz);
        if (builder == null) {
            builder = new EntityListenerClassInfoBuilder(listenerClazz, unitInfo);
            classToEntityListenerClassInfoBuilder.put(listenerClazz, builder);
        }
        builder.usingMappedClassInfos.add(info);
    }

    private List<Class<?>> orderHierarchy(List<Class<?>> original) {
        List<Class<?>> copy = new ArrayList<>(original);
        List<Class<?>> newList = new ArrayList<>();
        while (copy.size() > 0) {
            Class<?> clazz = copy.get(0);
            orderHierarchy(copy, newList, original, clazz);
        }
        return newList;
    }

    private void orderHierarchy(List<Class<?>> copy, List<Class<?>> newList, List<Class<?>> original, Class<?> clazz) {
        if (Object.class.equals(clazz)) return;
        // process superclass first
        orderHierarchy(copy, newList, original, clazz.getSuperclass());
        if (original.contains(clazz)) {
            if (!newList.contains(clazz)) {
                newList.add(clazz);
            }
            copy.remove(clazz);
        }
    }

    private List<Class<?>> addMappedSuperclasses(List<Class<?>> orderedClasses) {
        List<Class<?>> newOrderedClasses = new ArrayList<>(orderedClasses);
        for (Class<?> clazz : orderedClasses) {
            Class<?> superClazz = clazz.getSuperclass();
            if (!newOrderedClasses.contains(superClazz)) {
                addMappedSuperclasses(clazz, newOrderedClasses);
            }
        }
        return newOrderedClasses;
    }

    private void addMappedSuperclasses(Class<?> clazz, List<Class<?>> newOrderedClasses) {
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null) {
            if (superClass.isAnnotationPresent(MappedSuperclass.class)) {
                newOrderedClasses.add(0, superClass);
            }
            superClass = superClass.getSuperclass();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Class<?>, List<Class<?>>> readAnnotatedClasses(UnitInfo unitInfo,
                                                               Class<?>... annotationClasses)
            throws Exception {
        Map<Class<?>, List<Class<?>>> result = new HashMap<>();
        for (Class<?> annoClass : annotationClasses) {
            result.put(annoClass, new ArrayList<>());
        }
        List<ClasspathBrowser.Entry> entries = persistenceUnitParser.readEntries(unitInfo);
        for (ClasspathBrowser.Entry entry : entries) {
            ClassFile cf;
            if (entry.getInputStream() != null) {
                try (DataInputStream dstream = new DataInputStream(entry.getInputStream())) {
                    cf = new ClassFile(dstream);
                    AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.visibleTag);
                    if (visible != null) {
                        for (Class<?> annoClass : annotationClasses) {
                            javassist.bytecode.annotation.Annotation anno = visible.getAnnotation(annoClass.getName());
                            if (anno != null) {
                                List<Class<?>> list = result.get(annoClass);
                                list.add(persistenceUnitParser.loadClass(entry, cf.getName()));
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error during parsing class " + entry.getName(), e);
                } finally {
                    entry.getInputStream().close();
                }
            } else {
                @SuppressWarnings("rawtypes")
                Class candidateClass = getClass().getClassLoader().loadClass(entry.getName());
                for (Class<?> annoClass : annotationClasses) {
                    Annotation anno = candidateClass.getAnnotation(annoClass);
                    if (anno != null) {
                        List<Class<?>> list = result.get(annoClass);
                        list.add(candidateClass);
                    }
                }
            }
        }
        return result;
    }

    public List<UnitInfo> getUnitInfos() {
        return persistenceUnitParser.getUnitInfos();
    }

}
