package org.mariella.persistence.persistor;

import org.mariella.persistence.schema.ClassDescription;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterDescription implements Serializable {
    private final Map<String, PropertyChooser> propertyChoosers = new HashMap<>();
    private ClassDescription rootDescription;
    private String[] pathExpressions;

    public ClusterDescription() {
        super();
    }

    public ClusterDescription(ClassDescription rootDescription, String... pathExpressions) {
        super();
        this.rootDescription = rootDescription;
        this.pathExpressions = pathExpressions;
    }

    public ClassDescription getRootDescription() {
        return rootDescription;
    }

    public void setRootDescription(ClassDescription rootDescription) {
        this.rootDescription = rootDescription;
    }

    public String[] getPathExpressions() {
        return pathExpressions;
    }

    public void setPathExpressions(String[] pathExpressions) {
        this.pathExpressions = pathExpressions;
    }

    public PropertyChooser getPropertyChooser(String pathExpression) {
        return propertyChoosers.getOrDefault(pathExpression, PropertyChooser.All);
    }

    public void setPropertyChooser(String pathExpression, PropertyChooser propertyChooser) {
        propertyChoosers.put(pathExpression, propertyChooser);
    }

    public String[] getChildPathExpressions(String parentPathExpression) {
        List<String> children = new ArrayList<>();
        for (String pathExpression : pathExpressions) {
            if (pathExpression.length() > parentPathExpression.length() && pathExpression.startsWith(parentPathExpression)) {
                String child = pathExpression.substring(parentPathExpression.length() + 1);
                int idx = child.indexOf('.');
                if (idx > -1) {
                    child = child.substring(0, idx);
                }
                if (!children.contains(child)) {
                    children.add(child);
                }
            }
        }
        return children.toArray(new String[0]);
    }

}
