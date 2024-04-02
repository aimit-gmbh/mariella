package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.IntegerConverter;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.*;

import java.util.ArrayList;
import java.util.List;

public class OneToManyAttributeMappingBuilder extends ToManyAttributeMappingBuilder<OneToManyAttributeInfo> {

    public OneToManyAttributeMappingBuilder(EntityMappingBuilder entityMappingBuilder, OneToManyAttributeInfo attributeInfo) {
        super(entityMappingBuilder, attributeInfo);
    }

    protected void createBiDirectionalMappingWithoutJoinTable() {
        CollectionPropertyMapping pm;
        if (attributeInfo.getOrderByInfo() != null) {
            EntityMappingBuilder referencedEntityMappingBuilder = getEntityMappingBuilder().getPersistenceBuilder()
                    .getEntityMappingBuilder(referencedClassMapping);
            TableInfo referencedTableInfo = referencedEntityMappingBuilder.getTableInfoForForeignKeyColumn(
                    attributeInfo.getOrderByInfo().getOrderBy());
            Table referencedTable = referencedEntityMappingBuilder.getPersistenceBuilder().getTable(referencedTableInfo);
            Column orderByColumn = getEntityMappingBuilder().getPersistenceBuilder()
                    .getColumn(referencedTable, attributeInfo.getOrderByInfo().getOrderBy(), IntegerConverter.Singleton);
            pm = new CollectionPropertyMapping(getClassMapping(), getPropertyDescription(), orderByColumn);
        } else {
            pm = new CollectionPropertyMapping(getClassMapping(), getPropertyDescription());
        }
        getClassMapping().setPropertyMapping(getPropertyDescription(), pm);
        propertyMapping = pm;
    }

    protected void createUniDirectionalMappingWithoutJoinTable() {
        CollectionWithoutReferencePropertyMapping pm = new CollectionWithoutReferencePropertyMapping(getClassMapping(),
                getPropertyDescription());
        getClassMapping().setPropertyMapping(getPropertyDescription(), pm);
        propertyMapping = pm;

        EntityMappingBuilder referencedEntityMappingBuilder = getEntityMappingBuilder().getPersistenceBuilder()
                .getEntityMappingBuilder(referencedClassMapping);

        List<JoinColumn> joinColumns = new ArrayList<>();
        for (JoinColumnInfo jci : attributeInfo.getJoinColumnInfos()) {
            if (jci.getReferencedColumnName() == null || jci.getReferencedColumnName().length() == 0) {
                throw new IllegalStateException(
                        "No referenced column name specified for relationship " + getClassDescription().getClassName() + "."
                                + attributeInfo.getName() + "!");
            }
            TableInfo referencedTableInfo = referencedEntityMappingBuilder.getTableInfoForForeignKeyColumn(
                    jci.getReferencedColumnName());
            TableInfo referencedUpdateTableInfo = referencedEntityMappingBuilder.getUpdateTableInfoForForeignKeyColumn(
                    jci.getReferencedColumnName());
            Table referencedTable = getEntityMappingBuilder().getPersistenceBuilder().getTable(referencedTableInfo);
            Table referencedUpdateTable = getEntityMappingBuilder().getPersistenceBuilder().getTable(referencedUpdateTableInfo);

            Table myTable = getEntityMappingBuilder().getPersistenceBuilder()
                    .getTable(getEntityMappingBuilder().getTableInfo(attributeInfo));
            Table myUpdateTable = getEntityMappingBuilder().getPersistenceBuilder()
                    .getTable(getEntityMappingBuilder().getUpdateTableInfo(attributeInfo));

            Column myReadColumn = myTable.getColumn(jci.getName());
            if (myReadColumn == null) {
                throw new IllegalStateException("Join column " + myTable + "." + jci.getName() + " does not exist!");
            }

            Column myUpdateColumn = myUpdateTable.getColumn(jci.getName());
            if (myUpdateColumn == null) {
                throw new IllegalStateException("Join column " + myTable + "." + jci.getName() + " does not exist!");
            }

            Column referencedReadColumn = getEntityMappingBuilder().getPersistenceBuilder()
                    .getColumn(referencedTable, jci.getReferencedColumnName(), myReadColumn.converter());
            Column referencedUpdateColumn = null;
            if (jci.isInsertable() || jci.isUpdatable()) {
                referencedUpdateColumn = getEntityMappingBuilder().getPersistenceBuilder()
                        .getColumn(referencedUpdateTable, jci.getReferencedColumnName(), myUpdateColumn.converter());
            }
            JoinColumn jc = new JoinColumn();
            jc.setMyReadColumn(myReadColumn);
            jc.setMyUpdateColumn(myUpdateColumn);
            jc.setReferencedReadColumn(referencedReadColumn);
            jc.setReferencedUpdateColumn(referencedUpdateColumn);
            jc.setInsertable(jci.isInsertable());
            jc.setUpdatable(jci.isUpdatable());
            joinColumns.add(jc);
        }
        pm.setJoinColumns(joinColumns);
    }


}
