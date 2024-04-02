package org.mariella.persistence.loader;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Converter;
import org.mariella.persistence.database.ParameterValues;
import org.mariella.persistence.mapping.SchemaMapping;
import org.mariella.persistence.persistor.ClusterDescription;

import java.util.ArrayList;
import java.util.List;

public class AbstractClusterLoader implements ClusterLoader {
    protected final ClusterDescription clusterDescription;
    protected final SchemaMapping schemaMapping;
    protected final List<LoadingPolicy> loadingPolicies = new ArrayList<>();
    protected List<ClusterLoaderQueryParameter> queryParameters = new ArrayList<>();

    public AbstractClusterLoader(SchemaMapping schemaMapping, ClusterDescription clusterDescription) {
        super();
        this.schemaMapping = schemaMapping;
        this.clusterDescription = clusterDescription;

        if (clusterDescription.getPathExpressions().length == 0 || !clusterDescription.getPathExpressions()[0].equals("root")) {
            addLoadingPolicy("root");
        }
        for (String pathExpression : clusterDescription.getPathExpressions()) {
            addLoadingPolicy(pathExpression);
        }
    }

    private void addLoadingPolicy(String pathExpression) {
        LoadingPolicy policy = new LoadingPolicy(this, pathExpression);
        policy.setPropertyChooser(clusterDescription.getPropertyChooser(pathExpression));
        loadingPolicies.add(policy);
    }

    @Override
    public ClusterDescription getClusterDescription() {
        return clusterDescription;
    }

    @Override
    public SchemaMapping getSchemaMapping() {
        return schemaMapping;
    }

    public List<LoadingPolicy> getLoadingPolicies() {
        return loadingPolicies;
    }

    public void addParameter(Column column, Object value) {
        int index = queryParameters.size() + 1;
        queryParameters.add(new ClusterLoaderQueryParameter(column, value, index));
    }

    public void addParameter(Converter<?> converter, int sqlType, Object value) {
        int index = queryParameters.size() + 1;
        queryParameters.add(new ClusterLoaderQueryParameter(converter, sqlType, value, index));
    }

    protected void clearParameters() {
        queryParameters = new ArrayList<>();
    }

    protected static class ClusterLoaderQueryParameter {
        private final Converter<?> converter;
        @SuppressWarnings("unused")
        private final int sqlType;
        private final Object value;
        private final int index;

        public ClusterLoaderQueryParameter(Column column, Object value, int index) {
            this.converter = column.converter();
            this.sqlType = column.type();
            this.value = value;
            this.index = index;
        }

        public ClusterLoaderQueryParameter(Converter<?> converter, int sqlType, Object value, int index) {
            this.converter = converter;
            this.sqlType = sqlType;
            this.value = value;
            this.index = index;
        }

        @SuppressWarnings("unchecked")
        public void setParameter(ParameterValues pv) {
            ((Converter<Object>) converter).setObject(pv, index, value);
        }
    }


}
