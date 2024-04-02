package org.mariella.persistence.loader;

import org.mariella.persistence.mapping.SchemaMapping;
import org.mariella.persistence.persistor.ClusterDescription;

public interface ClusterLoader {

    SchemaMapping getSchemaMapping();

    ClusterDescription getClusterDescription();

}
