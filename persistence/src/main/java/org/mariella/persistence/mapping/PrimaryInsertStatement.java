package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.persistor.Persistor;
import org.mariella.persistence.runtime.ModifiableAccessor;

import java.util.List;
import java.util.function.Consumer;

public class PrimaryInsertStatement extends InsertStatement {
    private final ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor;

    public PrimaryInsertStatement(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor, Table table, List<Column> columns) {
        super(objectPersistor.getPersistor().getSchemaMapping().getSchema(), table, columns);
        this.objectPersistor = objectPersistor;
    }

    @Override
    public <T extends PreparedPersistorStatement> T prepare(Persistor<T> persistor) {
        ClassMapping classMapping = objectPersistor.getClassMapping();
        if (classMapping.getPrimaryKey().getGeneratedByDatabaseColumnMappings().length > 0) {
            return persistor.prepareStatement(this,
                    getSqlString(),
                    classMapping.getPrimaryKey().getGeneratedByDatabaseColumnNames()
            );
        } else {
            return super.prepare(persistor);
        }
    }

    @Override
    public Consumer<RowAndObject> getGeneratedColumnsCallback() {
        ClassMapping classMapping = objectPersistor.getClassMapping();
        if (classMapping.getPrimaryKey().getGeneratedByDatabaseColumnMappings().length == 0) {
            return null;
        } else {
            return
                    (RowAndObject row) -> {
                        int idx = 0;
                        for (ColumnMapping columnMapping : classMapping.getPrimaryKey().getGeneratedByDatabaseColumnMappings()) {
                            idx++;
                            Object value = columnMapping.getUpdateColumn().getObject(row.row(), idx);
                            ModifiableAccessor.Singleton.setValue(
                                    row.entity(),
                                    columnMapping.getPropertyDescription(),
                                    value);
                        }
                    };
        }
    }
}
