package org.mariella.persistence.mapping;


public class ClusterInfo {

    private Class<?> clusterClass;
    private String rootEntityName;
    private String[] pathExpressions;
    private String name;

    public Class<?> getClusterClass() {
        return clusterClass;
    }

    public void setClusterClass(Class<?> clusterClass) {
        this.clusterClass = clusterClass;
    }

    public String getRootEntityName() {
        return rootEntityName;
    }

    public void setRootEntityName(String rootEntityName) {
        this.rootEntityName = rootEntityName;
    }

    public String[] getPathExpressions() {
        return pathExpressions;
    }

    public void setPathExpressions(String[] pathExpressions) {
        this.pathExpressions = pathExpressions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
