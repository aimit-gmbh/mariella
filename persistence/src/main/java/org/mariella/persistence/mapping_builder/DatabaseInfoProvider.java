package org.mariella.persistence.mapping_builder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface DatabaseInfoProvider {

    DatabaseTableInfo getTableInfo(String catalog, String schema, String tableName);

    void load(ObjectInputStream is) throws IOException, ClassNotFoundException;

    void store(ObjectOutputStream os) throws IOException;
}
