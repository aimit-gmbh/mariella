package org.mariella.persistence.query;

import org.mariella.persistence.database.Column;

public class JoinedSecondaryTable extends JoinedTable {
    private boolean isReferencedByForeignKey = false;
    private SecondaryTableJoin join;

    public SecondaryTableJoin getJoin() {
        return join;
    }

    public void setJoin(SecondaryTableJoin join) {
        this.join = join;
        if (isReferencedByForeignKey) {
            join.markReferencedByForeignKey();
        }
    }

    @Override
    public ColumnReference createColumnReferenceForRelationship(Column foreignKeyColumn) {
        if (join == null) {
            isReferencedByForeignKey = true;
        } else {
            join.markReferencedByForeignKey();
        }
        return super.createColumnReferenceForRelationship(foreignKeyColumn);
    }

}
