package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;

public class JoinColumn {
    private Column myReadColumn;
    private Column myUpdateColumn;
    private Column referencedReadColumn;
    private Column referencedUpdateColumn;
    private boolean insertable;
    private boolean updatable;

    public Column getMyReadColumn() {
        return myReadColumn;
    }

    public void setMyReadColumn(Column myReadColumn) {
        this.myReadColumn = myReadColumn;
    }

    public Column getMyUpdateColumn() {
        return myUpdateColumn;
    }

    public void setMyUpdateColumn(Column myUpdateColumn) {
        this.myUpdateColumn = myUpdateColumn;
    }

    public Column getReferencedReadColumn() {
        return referencedReadColumn;
    }

    public void setReferencedReadColumn(Column referencedReadColumn) {
        this.referencedReadColumn = referencedReadColumn;
    }

    public Column getReferencedUpdateColumn() {
        return referencedUpdateColumn;
    }

    public void setReferencedUpdateColumn(Column referencedUpdateColumn) {
        this.referencedUpdateColumn = referencedUpdateColumn;
    }

    public boolean isInsertable() {
        return insertable;
    }

    public void setInsertable(boolean insertable) {
        this.insertable = insertable;
    }

    public boolean isUpdatable() {
        return updatable;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

}
