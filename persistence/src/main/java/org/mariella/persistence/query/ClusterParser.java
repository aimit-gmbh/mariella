package org.mariella.persistence.query;

import org.mariella.persistence.persistor.ClusterDescription;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.schema.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ClusterParser {
    private final ClusterDescription clusterDescription;
    private ClusterVisitor visitor;

    private List<String> currentPath;

    public ClusterParser(ClusterDescription clusterDescription) {
        super();
        this.clusterDescription = clusterDescription;
    }

    public ClusterDescription getClusterDescription() {
        return clusterDescription;
    }

    public ClusterVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(ClusterVisitor visitor) {
        this.visitor = visitor;
    }

    public List<String> getCurrentPath() {
        return currentPath;
    }

    public void parse(Object root) {
        for (String pathExpression : clusterDescription.getPathExpressions()) {
            currentPath = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(pathExpression, ".");
            while (tokenizer.hasMoreTokens()) {
                currentPath.add(tokenizer.nextToken());
            }
            visitor.beginPathExpression(pathExpression);
            visitor.root(root);
            FragmentParser parser = new FragmentParser(1, clusterDescription.getRootDescription(), root);
            parser.parse();
            visitor.endPathExpression(pathExpression);
        }
    }

    private void unknownOrAmbigousProperty(int pathIndex) {
        visitor.unkownOrAmbigousProperty(currentPath, pathIndex);
    }

    private void invalidProperty(int pathIndex) {
        visitor.invalidProperty(currentPath, pathIndex);
    }

    private class FragmentParser {
        private ClassDescription currentClassDescription;
        private int pathIndex;
        private Object currentEntity;

        public FragmentParser(int pathIndex, ClassDescription classDescription, Object currentEntity) {
            super();
            this.pathIndex = pathIndex;
            this.currentEntity = currentEntity;
            this.currentClassDescription = classDescription;
        }

        public void parse() {
            if (pathIndex < currentPath.size()) {
                String property = currentPath.get(pathIndex);
                PropertyDescription pd = currentClassDescription.getPropertyDescriptionInHierarchy(property);
                if (pd == null) {
                    unknownOrAmbigousProperty(pathIndex);
                } else if (!(pd instanceof RelationshipPropertyDescription rpd)) {
                    invalidProperty(pathIndex);
                } else {
                    Object value = ModifiableAccessor.Singleton.getValue(currentEntity, rpd);
                    if (value != null) {
                        if (!visitor.property(currentEntity, currentClassDescription, rpd, value)) {
                            return;
                        }
                        if (rpd instanceof ReferencePropertyDescription) {
                            currentClassDescription = rpd.getReferencedClassDescription();
                            currentEntity = value;
                            pathIndex++;
                            parse();
                        } else if (rpd instanceof CollectionPropertyDescription) {
                            List<?> list = (List<?>) value;
                            int index = 0;
                            for (Object entry : list) {
                                if (!visitor.indexedProperty(currentEntity, currentClassDescription,
                                        (CollectionPropertyDescription) rpd, index++, entry)) {
                                    return;
                                }
                                FragmentParser parser = new FragmentParser(pathIndex + 1, rpd.getReferencedClassDescription(),
                                        entry);
                                parser.parse();
                            }
                        } else {
                            throw new IllegalStateException();
                        }
                    }
                }
            }
        }
    }

}
