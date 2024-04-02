package org.mariella.persistence.query;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Table;

import java.util.ArrayList;
import java.util.List;

public class SubSelectBuilder {
    private final SubSelect subSelect = new SubSelect();

    private final List<TableReference> joinedTableReferences = new ArrayList<>();

    public SubSelectBuilder() {
        super();
    }

    public SubSelect getSubSelect() {
        return subSelect;
    }

    public List<TableReference> getJoinedTableReferences() {
        return joinedTableReferences;
    }

    public SelectItem addSelectItem(TableReference tableReference, Column column) {
        return addSelectItem(tableReference.createColumnReference(column));
    }

    public SelectItem addSelectItem(Expression expression) {
        SelectItem selectItem = new SelectItem();
        selectItem.setColumnReference(expression);
        subSelect.getSelectClause().getSelectItems().add(selectItem);
        return selectItem;
    }

    public Expression createCondition(final String expression) {
        return b -> b.append(expression);
    }

    public void and(Expression expression) {
        Expression condition = subSelect.getWhereClause().getCondition();
        if (condition == null) {
            subSelect.getWhereClause().setCondition(expression);
        } else {
            subSelect.getWhereClause().setCondition(BinaryCondition.and(condition, expression));
        }
    }

    public void or(Expression expression) {
        Expression condition = subSelect.getWhereClause().getCondition();
        if (condition == null) {
            subSelect.getWhereClause().setCondition(expression);
        } else {
            subSelect.getWhereClause().setCondition(BinaryCondition.or(condition, expression));
        }
    }

    public JoinedTable createJoinedTable(Table table) {
        JoinedTable joinedTable = new JoinedTable();
        joinedTable.setTable(table);
        addJoinedTable(joinedTable);
        return joinedTable;
    }

    public void addJoinedTable(JoinedTable joinedTable) {
        joinedTable.setAlias(createTableAlias(joinedTable.getTable().getName()));
        addJoinedTableReference(joinedTable);
    }

    public void addJoinedTableReference(TableReference tableReference) {
        joinedTableReferences.add(tableReference);
    }

    public String createSelectItemAlias(String nameBase) {
        int count = -1;
        String alias;
        if (nameBase.length() > DatabaseConstants.MAX_IDENTIFIER_LENGTH - 2) {
            nameBase = nameBase.substring(0, DatabaseConstants.MAX_IDENTIFIER_LENGTH - 2);
        }
        boolean found;
        do {
            if (count < 0) {
                count = 0;
                alias = nameBase;
            } else {
                alias = nameBase + count;
            }
            found = false;
            for (Expression expression : subSelect.getSelectClause().getSelectItems()) {
                if (expression instanceof SelectItem) {
                    if (((SelectItem) expression).getAlias().equals(alias)) {
                        found = true;
                        break;
                    }
                }
            }
            count++;
        } while (found);
        return alias;
    }

    public String createTableAlias(String nameBase) {
        int count = -1;
        String alias;
        if (nameBase.length() > DatabaseConstants.MAX_IDENTIFIER_LENGTH - 2) {
            nameBase = nameBase.substring(0, DatabaseConstants.MAX_IDENTIFIER_LENGTH - 2).replace('.', '_').replace('@', '_');
        }
        boolean found;
        do {
            if (count < 0) {
                count = 0;
                alias = nameBase;
            } else {
                alias = nameBase + count;
            }
            found = false;
            for (TableReference tableReference : joinedTableReferences) {
                if (tableReference.getAlias().equals(alias)) {
                    found = true;
                    break;
                }
            }
            count++;
        } while (found);
        return alias;
    }

    public TableReference getTableReference(String alias) {
        for (TableReference tr : joinedTableReferences) {
            if (tr.getAlias().equals(alias)) {
                return tr;
            }
        }
        return null;
    }

    public void addOrderBy(Expression expression) {
        getSubSelect().getOrderByClause().getItems().add(expression);
    }
}
