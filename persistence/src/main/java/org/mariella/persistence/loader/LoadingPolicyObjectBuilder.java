package org.mariella.persistence.loader;

import org.mariella.persistence.database.ResultSetReader;
import org.mariella.persistence.mapping.AbstractClassMapping;
import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.mapping.ObjectFactory;
import org.mariella.persistence.mapping.RelationshipPropertyMapping;
import org.mariella.persistence.query.DefaultPathExpressionVisitor;
import org.mariella.persistence.query.PathExpression;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.schema.PropertyDescription;
import org.mariella.persistence.schema.RelationshipPropertyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class LoadingPolicyObjectBuilder {
    private static final Logger logger = LoggerFactory.getLogger(LoadingPolicyObjectBuilder.class);
    protected final ObjectFactory objectFactory;
    protected final ResultSetReader resultSetReader;
    private final LoadingPolicy loadingPolicy;
    private final LoaderContext loaderContext;
    private final ClassMapping rootClassMapping;

    private ClassMapping currentClassMapping;
    private Object modifiable;

    public LoadingPolicyObjectBuilder(LoadingPolicy loadingPolicy, LoaderContext loaderContext, ResultSetReader resultSetReader) {
        super();
        this.loadingPolicy = loadingPolicy;
        this.loaderContext = loaderContext;
        this.resultSetReader = resultSetReader;
        this.objectFactory = new LoadingPolicyObjectFactory(loaderContext.isUpdate());
        this.rootClassMapping = loadingPolicy.getLoader().getSchemaMapping().getClassMapping(loadingPolicy.getLoader().getClusterDescription().getRootDescription().getClassName());
    }

    public List<?> createObjects() {
        try {
            if (logger.isTraceEnabled())
                logger.trace("### creating objects for loading policy: " + loadingPolicy.getPathExpression());
            final Set<Object> result = new LinkedHashSet<>();
            while (resultSetReader.next()) {
                resultSetReader.setCurrentColumnIndex(1);
                new PathExpression(loadingPolicy.getLoader().getSchemaMapping().getSchemaDescription(),
                        loadingPolicy.getPathExpression()).visit(
                        new DefaultPathExpressionVisitor() {
                            StringBuilder currentPathExpression;

                            public ClassDescription root(String token) {
                                currentPathExpression = new StringBuilder();
                                currentPathExpression.append(token);
                                if (logger.isTraceEnabled())
                                    logger.trace("currentPathExpression: " + currentPathExpression);
                                currentClassMapping = rootClassMapping;
                                if (logger.isTraceEnabled())
                                    logger.trace(
                                            "currentClassMapping: "
                                                    + currentClassMapping.getClassDescription().getClassName());
                                try {
                                    Object object = loadingPolicy.createObject(resultSetReader, currentClassMapping,
                                            objectFactory,
                                            currentPathExpression.toString().length() == loadingPolicy.getPathExpression()
                                                    .length());
                                    setModifiable(object);
                                } catch (SQLException e) {
                                    logger.error("Cannot create objects", e);
                                    throw new RuntimeException("Cannot create objects");
                                }
                                if (modifiable != null && !result.contains(modifiable)) {
                                    loaderContext.newObject(loadingPolicy.getLoader().getClusterDescription(),
                                            currentPathExpression.toString(), getClassDescription(modifiable),
                                            modifiable);
                                    result.add(modifiable);
                                }
                                return loadingPolicy.getLoader().getClusterDescription().getRootDescription();
                            }

                            @Override
                            public void property(ClassDescription classDescription,
                                                 PropertyDescription propertyDescription) {
                                if (modifiable != null) {
                                    currentPathExpression.append('.');
                                    currentPathExpression.append(propertyDescription.getPropertyDescriptor().getName());
                                    if (logger.isTraceEnabled())
                                        logger.trace(
                                                "currentPathExpression: " + currentPathExpression);
                                    currentClassMapping =
                                            ((RelationshipPropertyMapping) currentClassMapping
                                                    .getPropertyMappingInHierarchy(
                                                            propertyDescription)).getReferencedClassMapping();
                                    if (logger.isTraceEnabled())
                                        logger.trace(
                                                "currentClassMapping: "
                                                        + currentClassMapping.getClassDescription().getClassName());
                                    try {
                                        Object value = loadingPolicy.createObject(resultSetReader, currentClassMapping,
                                                objectFactory,
                                                currentPathExpression.toString().length() == loadingPolicy
                                                        .getPathExpression()
                                                        .length());
                                        if (value != null) {
                                            loaderContext.newObject(loadingPolicy.getLoader().getClusterDescription(),
                                                    currentPathExpression.toString(), getClassDescription(value), value);
                                        }
                                        if (logger.isTraceEnabled())
                                            logger.trace(
                                                    "adding to relationship: " + propertyDescription.getPropertyDescriptor()
                                                            .getName() + "\tvalue: "
                                                            + (value == null ? "null"
                                                            : currentClassMapping.getClassDescription()
                                                            .getId(value)));
                                        if (getClassDescription(modifiable).getPropertyDescriptions()
                                                .contains(propertyDescription)) {
                                            loaderContext.addToRelationship(modifiable,
                                                    (RelationshipPropertyDescription) propertyDescription, value);
                                        }
                                        setModifiable(value);
                                    } catch (Exception e) {
                                        logger.error("Cannot create objects", e);
                                        throw new RuntimeException("Cannot create objects", e);
                                    }
                                }
                            }

                            private void setModifiable(Object object) {
                                // 368008
                                LoadingPolicyObjectBuilder.this.modifiable = object;
                                if (modifiable == null) {
                                    if (logger.isTraceEnabled())
                                        logger.trace("setModifiable to null ");
                                } else {
                                    if (logger.isTraceEnabled())
                                        logger.trace("setModifiable instance of: " + object.getClass()
                                                .getName() + " id: "
                                                + currentClassMapping.getClassDescription().getId(object));
                                }
                            }
                        });
            }
            return new ArrayList<>(result);
        } catch (Exception e) {
            logger.error("Cannot create objects", e);
            throw new RuntimeException("Cannot create objects", e);
        }
    }

    private ClassDescription getClassDescription(Object modifiable) {
        return loadingPolicy.getLoader().getSchemaMapping().getSchemaDescription()
                .getClassDescription(modifiable.getClass().getName());
    }

    private final class LoadingPolicyObjectFactory implements ObjectFactory {
        private final boolean isUpdate;

        public LoadingPolicyObjectFactory(boolean isUpdate) {
            super();
            this.isUpdate = isUpdate;
        }

        public Object createObject(ClassMapping classMapping, Object identity) {
            return loaderContext.createModifiable(classMapping, identity);
        }

        public Object createEmbeddableObject(AbstractClassMapping classMapping) {
            return loaderContext.createEmbeddable(classMapping);
        }

        public Object getObject(ClassMapping classMapping, Object identity) {
            return loaderContext.getModifiable(identity);
        }

        public Object getValue(Object receiver, PropertyDescription propertyDescription) {
            return ModifiableAccessor.Singleton.getValue(receiver, propertyDescription);
        }

        public void setValue(Object receiver, PropertyDescription propertyDescription, Object value) {
            ModifiableAccessor.Singleton.setValue(receiver, propertyDescription, value);
        }

        public void updateValue(Object receiver, PropertyDescription propertyDescription, Object value) {
            if (isUpdate) {
                setValue(receiver, propertyDescription, value);
            }
        }
    }

}

