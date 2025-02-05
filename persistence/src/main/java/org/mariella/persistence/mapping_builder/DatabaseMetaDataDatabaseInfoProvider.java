package org.mariella.persistence.mapping_builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseMetaDataDatabaseInfoProvider implements DatabaseInfoProvider {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMetaDataDatabaseInfoProvider.class);


    private final DatabaseMetaData databaseMetaData;
    private List<DatabaseTableInfo> tableInfos = new ArrayList<>();

    private boolean ignoreSchema = false;
    private boolean usernameAsSchema = false;
    private boolean ignoreCatalog = false;

    public DatabaseMetaDataDatabaseInfoProvider(DatabaseMetaData databaseMetaData) {
        super();
        this.databaseMetaData = databaseMetaData;
    }

    @SuppressWarnings("unchecked")
    public void load(ObjectInputStream is) throws IOException, ClassNotFoundException {
        tableInfos = (List<DatabaseTableInfo>) is.readObject();
    }

    public void store(ObjectOutputStream os) throws IOException {
        os.writeObject(tableInfos);
    }

    private DatabaseMetaData getDatabaseMetaData() {
        if (databaseMetaData == null) {
            throw new RuntimeException("databaseMetaData is not loaded!!!");
        }
        return databaseMetaData;
    }

    public DatabaseTableInfo getTableInfo(String catalog, String schema, String tableName) {
        for (DatabaseTableInfo dti : tableInfos) {
            if ((ignoreCatalog || equals(catalog, dti.getCatalog())) && (ignoreSchema || equals(schema,
                    dti.getSchema())) && equals(tableName, dti.getName())) {
                return dti;
            }
        }
        return loadTableInfo(catalog, schema, tableName);
    }

    private boolean equals(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        } else if (s1 == null && s2.isEmpty()) {
            return true;
        } else if (s2 == null && s1.isEmpty()) {
            return true;
        } else if (s1 == null || s2 == null) {
            return false;
        } else {
            return s1.equalsIgnoreCase(s2);
        }
    }

    public DatabaseTableInfo loadTableInfo(String catalog, String schema, String tableName) {
        try {
            if (ignoreCatalog || (catalog != null && catalog.isEmpty())) {
                catalog = null;
            }
            if (ignoreSchema || (schema != null && schema.isEmpty())) {
                schema = null;
            }
            if (!ignoreSchema && usernameAsSchema && schema == null) {
                schema = getDatabaseMetaData().getUserName();
                if (schema != null && schema.isEmpty()) {
                    schema = null;
                }
            }
            try (ResultSet rs = getDatabaseMetaData().getTables(catalog, schema, tableName, null)) {
                if (rs.next()) {
                    DatabaseTableInfo tableInfo = new DatabaseTableInfo();
                    tableInfo.setCatalog(rs.getString(1));
                    tableInfo.setSchema(rs.getString(2));
                    tableInfo.setName(rs.getString(3));

                    if ((ignoreSchema || equals(tableInfo.getSchema(), schema)) && (ignoreCatalog || equals(
                            tableInfo.getCatalog(), catalog))) {
                        loadColumnInfos(tableInfo);
                        loadPrimaryKey(tableInfo);
                        tableInfos.add(tableInfo);
                        return tableInfo;
                    }
                }
                return null;
            }
        } catch (SQLException e) {
            String message = "Failed to load table " + tableName;
            LOGGER.error(message, e);
            throw new RuntimeException(message);
        }
    }

    private void loadColumnInfos(DatabaseTableInfo tableInfo) throws SQLException {
        try (ResultSet rs = getDatabaseMetaData().getColumns(tableInfo.getCatalog(), tableInfo.getSchema(), tableInfo.getName(),
                null)) {
            while (rs.next()) {
                DatabaseColumnInfo info = new DatabaseColumnInfo();
                info.setName(rs.getString(4));
                info.setType(rs.getInt(5));
                int i;
                rs.getInt(7);
                if (!rs.wasNull()) {
                    info.setLength(7);
                }
                i = rs.getInt(9);
                if (!rs.wasNull()) {
                    info.setScale(i);
                }
                int nullable = rs.getInt(11);
                if (nullable == DatabaseMetaData.attributeNullableUnknown) {
                    throw new RuntimeException(
                            "Cannot determine nullable for column " + tableInfo.getName() + "." + info.getName() + "!");
                }
                info.setNullable(nullable == DatabaseMetaData.attributeNullable);
                tableInfo.addColumnInfo(info);
            }
        }
    }

    private void loadPrimaryKey(DatabaseTableInfo tableInfo) throws SQLException {
        try (ResultSet rs = getDatabaseMetaData().getPrimaryKeys(tableInfo.getCatalog(), tableInfo.getSchema(),
                tableInfo.getName())) {
            while (rs.next()) {
                String columnName = rs.getString(4);
                DatabaseColumnInfo columnInfo = tableInfo.getColumnInfo(columnName);
                if (columnInfo == null) {
                    throw new IllegalStateException("Unkown column for primary key");
                }
                tableInfo.getPrimaryKey().add(columnInfo);
            }
        }


    }

    public boolean isIgnoreSchema() {
        return ignoreSchema;
    }

    public void setIgnoreSchema(boolean ignoreSchema) {
        this.ignoreSchema = ignoreSchema;
    }

    public boolean isIgnoreCatalog() {
        return ignoreCatalog;
    }

    public void setIgnoreCatalog(boolean ignoreCatalog) {
        this.ignoreCatalog = ignoreCatalog;
    }

    public boolean isUsernameAsSchema() {
        return usernameAsSchema;
    }

    public void setUsernameAsSchema(boolean usernameAsSchema) {
        this.usernameAsSchema = usernameAsSchema;
    }

}
