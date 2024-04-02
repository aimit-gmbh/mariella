package org.mariella.persistence.database;

public interface ResultSetReader {
    int getCurrentColumnIndex();

    void setCurrentColumnIndex(int index);

    ResultRow getResultRow();

    boolean next();
}
