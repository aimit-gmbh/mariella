package org.mariella.persistence.database;

import org.mariella.persistence.mapping.JoinedClassMapping;
import org.mariella.persistence.mapping.PersistorStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Schema {
    private final Map<String, Table> tables = new HashMap<>();
    private final Map<String, Sequence> sequences = new HashMap<>();

    private Class<?> parameterClass = JdbcParameter.class;

    public abstract PersistorStatement createJoinedUpsertStatement(JoinedClassMapping joinedClassMapping, List<Column> columns);

    public abstract void addBatch(PreparedStatement ps) throws SQLException;

    public Collection<Table> getTables() {
        return tables.values();
    }

    public Table getTable(String name) {
        return tables.get(name);
    }

    public void addTable(Table table) {
        tables.put(table.getName(), table);
    }

    public Collection<Sequence> getSequences() {
        return sequences.values();
    }

    public Sequence getSequence(String name) {
        return sequences.get(name);
    }

    public void addSequence(Sequence sequence) {
        sequences.put(sequence.getName(), sequence);
    }

    public Class<?> getParameterClass() {
        return parameterClass;
    }

    public void setParameterClass(Class<?> parameterClass) {
        this.parameterClass = parameterClass;
    }

    public Parameter createParameter(int parameterIndex) {
        try {
            return (Parameter) parameterClass.getConstructor(int.class).newInstance(parameterIndex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
