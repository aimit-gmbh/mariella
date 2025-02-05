package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Converter;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.database.ResultSetReader;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.persistor.Row;
import org.mariella.persistence.query.*;
import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.util.InitializationHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class SelectableHierarchyClassMapping extends ClassMapping {
    private final List<SelectableHierarchyClassMapping> containedChildren = new ArrayList<>();
    private final List<SelectableHierarchyClassMapping> selectableContainedChildren =
            new ArrayList<>();
    private Column discriminatorColumn;
    private Object discriminatorValue;
    private SelectableHierarchyClassMapping containingClassMapping;
    private List<PropertyMapping> containedPropertyMappings;

    public SelectableHierarchyClassMapping(SchemaMapping schemaMapping, ClassDescription classDescription) {
        super(schemaMapping, classDescription);
    }

    public void setDiscriminatorColumn(Column discriminatorColumn) {
        this.discriminatorColumn = discriminatorColumn;
    }

    protected abstract boolean shouldBeContainedBy(ClassMapping classMapping);

    @Override
    public void initialize(InitializationHelper<ClassMapping> context) {
        super.initialize(context);
        if (getSuperClassMapping() != null && shouldBeContainedBy(getSuperClassMapping())) {
            containingClassMapping = (SelectableHierarchyClassMapping) getSuperClassMapping();
            discriminatorColumn = ((SelectableHierarchyClassMapping) getSuperClassMapping()).getDiscriminatorColum();
            containingClassMapping.addContainedClassMapping(this);
        } else {
            containingClassMapping = null;
        }
    }

    protected void addContainedClassMapping(SelectableHierarchyClassMapping classMapping) {
        if (!containedChildren.contains(classMapping)) {
            containedChildren.add(classMapping);
            if (!classMapping.getClassDescription().isAbstract()) {
                selectableContainedChildren.add(classMapping);
            }
            if (containingClassMapping != null) {
                containingClassMapping.addContainedClassMapping(classMapping);
            }
        }
    }

    public List<SelectableHierarchyClassMapping> getContainedChildren() {
        return containedChildren;
    }

    public SelectableHierarchyClassMapping getContainingClassMapping() {
        return containingClassMapping;
    }

    protected boolean isSelectableHierarchyRoot() {
        return containingClassMapping == null;
    }

    public Column getDiscriminatorColum() {
        return discriminatorColumn;
    }

    @Override
    public Object getDiscriminatorValue() {
        return discriminatorValue == null ? getClassDescription().getClassName() : discriminatorValue;
    }

    public void setDiscriminatorValue(Object discriminatorValue) {
        this.discriminatorValue = discriminatorValue;
    }

    public List<PropertyMapping> getContainedPropertyMappings() {
        return containedPropertyMappings;
    }

    public void postInitialize(InitializationHelper<ClassMapping> context) {
        super.postInitialize(context);

        containedPropertyMappings = new ArrayList<>();

        containedPropertyMappings.addAll(getPropertyMappings());

        for (ClassMapping child : getImmediateChildren()) {
            if (child instanceof SelectableHierarchyClassMapping hChild) {
                if (!hChild.isSelectableHierarchyRoot()) {
                    context.ensureInitialized(hChild);
                    for (PropertyMapping pm : hChild.getContainedPropertyMappings()) {
                        if (!containedPropertyMappings.contains(pm)) {
                            containedPropertyMappings.add(pm);
                        }
                    }
                }
            }
        }
    }

    public void collectUsedColumns(Collection<Column> collection) {
        super.collectUsedColumns(collection);
        if (discriminatorColumn != null && !collection.contains(discriminatorColumn)) {
            collection.add(discriminatorColumn);
        }
    }

    @Override
    protected boolean needsSubSelect() {
        return getAllChildren().size() > getContainedChildren().size();
    }

    @Override
    protected boolean needsSubSelect(ClassMapping childMapping) {
        return !containedChildren.contains(childMapping);
    }

    @Override
    public SelectItem addDiscriminatorColumn(SubSelectBuilder subSelectBuilder, TableReference tableReference) {
        if (getDiscriminatorColum() != null) {
            return subSelectBuilder.addSelectItem(tableReference, getDiscriminatorColum());
        } else {
            return subSelectBuilder.addSelectItem(new StringLiteral((String) getDiscriminatorValue()));
        }
    }

    @Override
    public Object getDiscriminatorValue(ResultSetReader reader) {
        return getDiscriminatorColum().getObject(reader.getResultRow(), reader.getCurrentColumnIndex());
    }

    @Override
    public void registerDiscriminator(HierarchySubSelect hierarchySubSelect) {
        if (getDiscriminatorColum() == null) {
            hierarchySubSelect.registerAnonymousDiscriminator();
        } else {
            hierarchySubSelect.registerDiscriminatorColumn(getDiscriminatorColum());
        }
    }

    @Override
    protected void collectJoinBuilderSubSelects(HierarchySubSelect hierarchySubSelect,
                                                List<PropertyMapping> propertyMappingList) {
        if (getContainingClassMapping() == null) {
            createJoinBuilderSubSelect(hierarchySubSelect, propertyMappingList);
        }

        for (ClassMapping child : getImmediateChildren()) {
            child.collectJoinBuilderSubSelects(hierarchySubSelect, propertyMappingList);
        }
    }

    protected boolean maySelectForHierarchy(PropertyMapping propertyMapping) {
        return getContainedPropertyMappings().contains(propertyMapping);
    }

    @Override
    protected JoinBuilder primitiveCreateJoinBuilder(SubSelectBuilder subSelectBuilder) {
        List<SelectableHierarchyClassMapping> selected = new ArrayList<>();
        selected.add(this);
        selected.addAll(selectableContainedChildren);

        List<SelectableHierarchyClassMapping> affected = new ArrayList<>(containedChildren);

        return primitiveCreateJoinBuilder(subSelectBuilder, affected, selected);
    }

    @SuppressWarnings("unchecked")
    protected JoinBuilder primitiveCreateJoinBuilder(SubSelectBuilder subSelectBuilder,
                                                     List<SelectableHierarchyClassMapping> affectedChildren,
                                                     List<SelectableHierarchyClassMapping> selectedMappings) {
        JoinBuilder joinBuilder = super.primitiveCreateJoinBuilder(subSelectBuilder);

        if (!isSelectableHierarchyRoot() || selectedMappings.size() != selectableContainedChildren.size() + 1) {
            if (selectedMappings.isEmpty()) {
                joinBuilder.getConditionBuilder(getDiscriminatorColum()).and(
                        BinaryCondition.eq(
                                joinBuilder.getJoinedTableReference().createColumnReference(getDiscriminatorColum()),
                                ((Converter<Object>) getDiscriminatorColum().converter()).createLiteral(
                                        getDiscriminatorValue())));
            } else {
                List<Expression> in = new ArrayList<>();
                for (SelectableHierarchyClassMapping child : selectedMappings) {
                    if (!child.getClassDescription().isAbstract()) {
                        in.add(((Converter<Object>) getDiscriminatorColum().converter()).createLiteral(
                                child.getDiscriminatorValue()));
                    }
                }
                joinBuilder.getConditionBuilder(getDiscriminatorColum()).and(
                        new InCondition(
                                joinBuilder.getJoinedTableReference().createColumnReference(getDiscriminatorColum()),
                                in));
            }
        }

        return joinBuilder;
    }

    @Override
    public void initializePrimaryRow(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor, Row row) {
        initializePrimaryRow(objectPersistor, row, getDiscriminatorValue());
    }

    public void initializePrimaryRow(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor, Row row, Object discriminatorValue) {
        super.initializePrimaryRow(objectPersistor, row);
        if (discriminatorColumn != null && primaryUpdateTable.getColumn(discriminatorColumn.name()) != null) {
            row.setProperty(discriminatorColumn, discriminatorValue);
        }
    }
}
