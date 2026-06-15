package org.mariella.persistence.loader;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Converter;
import org.mariella.persistence.loader.AbstractClusterLoader.ClusterLoaderQueryParameter;
import org.mariella.persistence.mapping.SchemaMapping;
import org.mariella.persistence.persistor.ClusterDescription;

public interface ClusterLoader {

    SchemaMapping getSchemaMapping();

    ClusterDescription getClusterDescription();
    
    public void addParameter(Column column, Object value);
    public void addParameter(Converter<?> converter, int sqlType, Object value);


}
