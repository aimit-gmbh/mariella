package org.mariella.persistence.mapping;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKeyJoinColumns {
    private final List<PrimaryKeyJoinColumn> primaryKeyJoinColumns = new ArrayList<>();


    public void addPrimaryKeyJoinColumn(PrimaryKeyJoinColumn primaryKeyJoinColumn) {
        primaryKeyJoinColumns.add(primaryKeyJoinColumn);
    }

    public List<PrimaryKeyJoinColumn> getPrimaryKeyJoinColumns() {
        return primaryKeyJoinColumns;
    }

}
