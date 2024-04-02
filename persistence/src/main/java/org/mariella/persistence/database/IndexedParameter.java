package org.mariella.persistence.database;

public class IndexedParameter implements Parameter {
    private final int index;

    public IndexedParameter(int index) {
        this.index = index;
    }

    @Override
    public void print(StringBuilder b) {
        b.append("$").append(index);
    }
}
