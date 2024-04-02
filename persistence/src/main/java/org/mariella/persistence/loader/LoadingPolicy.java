package org.mariella.persistence.loader;

import org.mariella.persistence.database.ResultSetReader;
import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.mapping.ObjectFactory;
import org.mariella.persistence.persistor.PropertyChooser;
import org.mariella.persistence.query.QueryBuilder;
import org.mariella.persistence.query.TableReference;

import java.sql.SQLException;

public class LoadingPolicy {
    private final ClusterLoader loader;
    private final String pathExpression;
    private PropertyChooser propertyChooser = PropertyChooser.All;

    public LoadingPolicy(ClusterLoader loader, String pathExpression) {
        super();
        this.loader = loader;
        this.pathExpression = pathExpression;
    }

    public ClusterLoader getLoader() {
        return loader;
    }

    public String getPathExpression() {
        return pathExpression;
    }

    public PropertyChooser getPropertyChooser() {
        return propertyChooser;
    }

    public void setPropertyChooser(PropertyChooser propertyChooser) {
        this.propertyChooser = propertyChooser;
    }

    public void addObjectSelectItems(QueryBuilder queryBuilder, ClassMapping classMapping, TableReference tableReference) {
        classMapping.addObjectColumns(queryBuilder, tableReference, propertyChooser);
    }

    public void addIdentitySelectItems(QueryBuilder queryBuilder, ClassMapping classMapping, TableReference tableReference) {
        classMapping.addIdentityColumns(queryBuilder, tableReference);
    }

    public Object createObject(ResultSetReader reader, ClassMapping classMapping, ObjectFactory factory,
                               boolean wantsObjects)
            throws SQLException {
        return classMapping.createObject(reader, factory, wantsObjects, propertyChooser);
    }

}
