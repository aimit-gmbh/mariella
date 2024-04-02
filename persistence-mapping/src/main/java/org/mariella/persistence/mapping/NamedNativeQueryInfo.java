package org.mariella.persistence.mapping;


public class NamedNativeQueryInfo {

    private String name;
    private String query;
    private Class<?> resultClass;
    private QueryHintInfo[] queryHintInfos;
    private String sqlResultSetMappingName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Class<?> getResultClass() {
        return resultClass;
    }

    public void setResultClass(Class<?> resultClass) {
        this.resultClass = resultClass;
    }

    public QueryHintInfo[] getQueryHintInfos() {
        return queryHintInfos;
    }

    public void setQueryHintInfos(QueryHintInfo[] queryHintInfos) {
        this.queryHintInfos = queryHintInfos;
    }

    public String getSqlResultSetMappingName() {
        return sqlResultSetMappingName;
    }

    public void setSqlResultSetMappingName(String sqlResultSetMappingName) {
        this.sqlResultSetMappingName = sqlResultSetMappingName;
    }


}
