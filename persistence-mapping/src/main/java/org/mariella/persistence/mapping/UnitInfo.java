package org.mariella.persistence.mapping;

import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

import javax.sql.DataSource;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;

public class UnitInfo implements PersistenceUnitInfo {

    final List<URL> jarFileUrls = new ArrayList<>();
    final List<String> managedClassNames = new ArrayList<>();
    final Properties properties = new Properties();
    final List<ClusterInfo> clusterInfos = new ArrayList<>();
    final List<SqlResultSetMappingInfo> sqlResultSetMappingInfos = new ArrayList<>();
    final List<NamedNativeQueryInfo> namedNativeQueryInfos = new ArrayList<>();
    String persistenceUnitName = null;
    URL persistenceUnitRootUrl = null;
    Map<String, ClassInfo> classToInfoMap = new HashMap<>();
    List<ClassInfo> hierarchyOrderedClassInfos = new ArrayList<>();
    List<SequenceGeneratorInfo> sequenceGeneratorInfos = new ArrayList<>();
    List<TableGeneratorInfo> tableGeneratorInfos = new ArrayList<>();
    List<DomainDefinitionInfo> domainDefinitionInfos = new ArrayList<>();

    public Collection<ClassInfo> getClassInfos() {
        return classToInfoMap.values();
    }


    public void addTransformer(ClassTransformer transformer) {
        throw new UnsupportedOperationException();
    }

    public boolean excludeUnlistedClasses() {
        throw new UnsupportedOperationException();
    }

    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException();
    }

    public List<URL> getJarFileUrls() {
        return jarFileUrls;
    }

    public DataSource getJtaDataSource() {
        throw new UnsupportedOperationException();
    }

    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    public List<String> getMappingFileNames() {
        throw new UnsupportedOperationException();
    }

    public ClassLoader getNewTempClassLoader() {
        throw new UnsupportedOperationException();
    }

    public DataSource getNonJtaDataSource() {
        throw new UnsupportedOperationException();
    }

    public String getPersistenceProviderClassName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getScopeAnnotationName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getQualifierAnnotationNames() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("removal")
    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        throw new UnsupportedOperationException();
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitRootUrl;
    }

    public void setPersistenceUnitRootUrl(URL persistenceUnitRootUrl) {
        this.persistenceUnitRootUrl = persistenceUnitRootUrl;
    }

    public Properties getProperties() {
        return properties;
    }

    public String toString() {
        return "UnitInfo name: " + getPersistenceUnitName();
    }

    public void debugPrint(PrintStream out) {
        out.println("======= START PERSISTENCE UNIT INFO ========");
        out.println("[persistenceUnitRootURL: " + persistenceUnitRootUrl + "]");
        for (ClassInfo info : classToInfoMap.values()) {
            out.println();
            info.debugPrint(out);
        }
        out.println("======= END PERSISTENCE UNIT INFO ========");
    }

    public List<ClusterInfo> getClusterInfos() {
        return clusterInfos;
    }

    public List<SqlResultSetMappingInfo> getSqlResultSetMappingInfos() {
        return sqlResultSetMappingInfos;
    }

    public List<NamedNativeQueryInfo> getNamedNativeQueryInfos() {
        return namedNativeQueryInfos;
    }

    public ClassInfo getClassInfo(Class<?> clazz) {
        return classToInfoMap.get(clazz.getName());
    }

    public ClassInfo getClassInfo(String className) {
        return classToInfoMap.get(className);
    }

    public List<SequenceGeneratorInfo> getSequenceGeneratorInfos() {
        return sequenceGeneratorInfos;
    }

    public void setSequenceGeneratorInfos(
            List<SequenceGeneratorInfo> sequenceGeneratorInfos) {
        this.sequenceGeneratorInfos = sequenceGeneratorInfos;
    }

    public List<TableGeneratorInfo> getTableGeneratorInfos() {
        return tableGeneratorInfos;
    }

    public void setTableGeneratorInfos(List<TableGeneratorInfo> tableGeneratorInfos) {
        this.tableGeneratorInfos = tableGeneratorInfos;
    }

    public List<DomainDefinitionInfo> getDomainDefinitionInfos() {
        return domainDefinitionInfos;
    }

    public void setDomainDefinitionInfos(
            List<DomainDefinitionInfo> domainDefinitionInfos) {
        this.domainDefinitionInfos = domainDefinitionInfos;
    }

    public List<ClassInfo> getHierarchyOrderedClassInfos() {
        return hierarchyOrderedClassInfos;
    }

    public void setHierarchyOrderedClassInfos(
            List<ClassInfo> hierarchyOrderedClassInfos) {
        this.hierarchyOrderedClassInfos = hierarchyOrderedClassInfos;
    }

    public Map<String, ClassInfo> getClassToInfoMap() {
        return classToInfoMap;
    }

    public void setClassToInfoMap(Map<String, ClassInfo> classToInfoMap) {
        this.classToInfoMap = classToInfoMap;
    }

    public String getPersistenceXMLSchemaVersion() {
        return "1.0";
    }

    public ValidationMode getValidationMode() {
        return ValidationMode.NONE;
    }

    public SharedCacheMode getSharedCacheMode() {
        return SharedCacheMode.NONE;
    }

}
