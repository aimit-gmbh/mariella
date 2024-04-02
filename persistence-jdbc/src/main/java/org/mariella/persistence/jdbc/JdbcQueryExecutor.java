package org.mariella.persistence.jdbc;

import org.mariella.persistence.database.Converter;
import org.mariella.persistence.persistor.DatabaseAccess;
import org.mariella.persistence.query.Expression;
import org.mariella.persistence.query.QueryBuilder;
import org.mariella.persistence.query.ScalarExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcQueryExecutor {
    private static final Logger logger = LoggerFactory.getLogger(JdbcQueryExecutor.class);
    
    private final QueryBuilder queryBuilder;
    private final DatabaseAccess databaseAccess;

    public JdbcQueryExecutor(QueryBuilder queryBuilder, DatabaseAccess databaseAccess) {
        this.queryBuilder = queryBuilder;
        this.databaseAccess = databaseAccess;
    }

    protected void setParameters(PreparedStatement ignored) {
    }

    public Object getObject(ResultSet rs, int index) throws SQLException {
        Expression e = queryBuilder.getSubSelect().getSelectClause().getSelectItems().get(index - 1);
        if (e instanceof ScalarExpression) {
            Converter<?> converter = ((ScalarExpression) e).getConverter();
            if (converter != null) {
                return converter.getObject(new JdbcResultRow(rs), index);
            }
        }
        return rs.getObject(index);
    }

    public Object queryforObject() throws SQLException {
        List<Object[]> result = queryForObjects();
        if (result.size() != 1) {
            throw new RuntimeException("Expected a single result. Actual result size is " + result.size());
        }
        if (result.get(0).length != 1) {
            throw new RuntimeException("The result row must contain a single value. The actual result row contains " + result.get(
                    0).length + " values");
        }
        return result.get(0)[0];
    }

    public List<Object[]> queryForObjects() throws SQLException {
        final List<Object[]> result = new ArrayList<>();
        query(rs -> {
            Object[] values = new Object[queryBuilder.getSubSelect().getSelectClause().getSelectItems().size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = getObject(rs, i + 1);
            }
            result.add(values);
        });
        return result;
    }

    public void query(RowCallbackHandler rowCallbackHandler) throws SQLException {
        databaseAccess.doInConnection(
                connection -> {
                    StringBuilder b = new StringBuilder();
                    queryBuilder.getSubSelect().printSql(b);
                    if (logger.isDebugEnabled())
                        logger.debug(b.toString());
                    try (PreparedStatement ps = connection.prepareStatement(b.toString())) {
                        setParameters(ps);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                rowCallbackHandler.processRow(rs);
                            }
                        }
                    }
                    return null;
                });
    }

    public interface RowCallbackHandler {
        void processRow(ResultSet rs) throws SQLException;
    }


}
