package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.StringConverter;
import org.mariella.persistence.query.*;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HierarchySubSelect implements TableReference {
    private final List<SubSelectBuilder> subSelectBuilders = new ArrayList<>();
    private final Map<Column, Column> virtualTableColumnMap = new HashMap<>();
    private String alias;
    private Column discriminiatorColum;
    private boolean referenced = false;

    public HierarchySubSelect() {
        super();
    }

    @Override
    public boolean isReferenced() {
        return referenced;
    }

    public void addSubSelectBuilder(SubSelectBuilder subSelectBuilder) {
        subSelectBuilders.add(subSelectBuilder);
    }

    public Column registerDiscriminatorColumn(Column discriminator) {
        if (discriminiatorColum == null) {
            discriminiatorColum = new Column("D", discriminator.type(), false, discriminator.converter());
        }
        virtualTableColumnMap.put(discriminator, discriminiatorColum);
        return discriminiatorColum;
    }

    public Column registerAnonymousDiscriminator() {
        if (this.discriminiatorColum == null) {
            this.discriminiatorColum = new Column("D", Types.VARCHAR, false, StringConverter.Singleton);
        }
        return this.discriminiatorColum;
    }

    public void selectColumn(SubSelectBuilder subSelectBuilder, TableReference tableReference, Column readColumn) {
        String alias;
        Column virtualColumn = virtualTableColumnMap.get(readColumn);

        if (virtualColumn != null) {
            alias = virtualColumn.name();
        } else {
            alias = subSelectBuilder.createSelectItemAlias(readColumn.name());
            Column column = new Column(alias, readColumn.type(), readColumn.nullable(), readColumn.converter());
            virtualTableColumnMap.put(readColumn, column);
        }
        SelectItem selectItem = subSelectBuilder.addSelectItem(new ColumnReferenceImpl(tableReference, readColumn));
        selectItem.setAlias(alias);
    }

    public void selectDummy(SubSelectBuilder subSelectBuilder, TableReference tableReference, Column readColumn) {
        String alias;
        Column virtualColumn = virtualTableColumnMap.get(readColumn);

        if (virtualColumn != null) {
            alias = virtualColumn.name();
            virtualTableColumnMap.put(readColumn, virtualColumn);
        } else {
            alias = subSelectBuilder.createSelectItemAlias(readColumn.name());
            Column column = new Column(alias, readColumn.type(), readColumn.nullable(), readColumn.converter());
            virtualTableColumnMap.put(readColumn, column);
        }
        SelectItem selectItem = subSelectBuilder.addSelectItem(readColumn.converter().createDummy());
        selectItem.setAlias(alias);
    }

    @Override
    public ColumnReference createUnreferencedColumnReference(Column column) {
        Column virtualColumn = virtualTableColumnMap.get(column);
        if (virtualColumn == null) {
            throw new IllegalArgumentException();
        } else {
            return new ColumnReferenceImpl(this, virtualColumn);
        }
    }

    @Override
    public ColumnReference createColumnReference(Column column) {
        referenced = true;
        return createUnreferencedColumnReference(column);
    }

    @Override
    public ColumnReference createColumnReferenceForRelationship(Column foreignKeyColumn) {
        return createColumnReference(foreignKeyColumn);
    }

    @Override
    public boolean canCreateColumnReference(Column column) {
        return virtualTableColumnMap.containsKey(column);
    }

    @Override
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public void printFromClause(StringBuilder b) {
        b.append("(");
        boolean first = true;
        for (SubSelectBuilder subSelectBuilder : subSelectBuilders) {
            if (first) {
                first = false;
            } else {
                b.append(" union all ");
            }
            subSelectBuilder.getSubSelect().printSql(b);
        }
        b.append(") ");
        b.append(alias);
    }

    @Override
    public void printSql(StringBuilder b) {
        b.append(alias);
    }

}
