package org.mariella.persistence.query;

import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.schema.PropertyDescription;
import org.mariella.persistence.schema.RelationshipPropertyDescription;
import org.mariella.persistence.schema.SchemaDescription;

import java.util.StringTokenizer;


public class PathExpression {
    private final SchemaDescription schemaDescription;
    private final String expression;

    public PathExpression(SchemaDescription schemaDescription, String expression) {
        super();
        this.schemaDescription = schemaDescription;
        this.expression = expression;
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PathExpression && expression.equals(((PathExpression) obj).expression);
    }

    public String toString() {
        return expression;
    }

    public void visit(PathExpressionVisitor visitor) {
        ClassDescription currentClassDescription = null;
        PropertyDescription currentPropertyDescription = null;
        StringTokenizer tokenizer = new StringTokenizer(expression, ".");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (currentClassDescription == null) {
                if (currentPropertyDescription != null) {
                    visitor.afterEnd(token);
                } else {
                    if (token.indexOf(':') > -1) {
                        String castedClassName = token.substring(0, token.indexOf(':'));
                        ClassDescription castTo = schemaDescription.getClassDescription(castedClassName);
                        if (castTo == null) {
                            visitor.unknownType(castedClassName);
                        }
                        String root = token.substring(token.indexOf(':') + 1);
                        currentClassDescription = visitor.root(root);

                        if (castTo != null && !castTo.isA(currentClassDescription)) {
                            visitor.invalidCast(currentClassDescription, castTo);
                        }
                        currentClassDescription = castTo;
                    } else {
                        currentClassDescription = visitor.root(token);
                    }
                }
            } else {
                String propertyName;
                if (token.indexOf(':') > -1) {
                    String castedClassName = token.substring(0, token.indexOf(':'));
                    propertyName = token.substring(token.indexOf(':') + 1);
                    ClassDescription castTo = schemaDescription.getClassDescription(castedClassName);
                    if (castTo == null) {
                        visitor.unknownType(castedClassName);
                    } else if (!castTo.isA(currentClassDescription)) {
                        visitor.invalidCast(currentClassDescription, castTo);
                    }
                    currentClassDescription = castTo;
                } else {
                    propertyName = token;
                }
                if (currentClassDescription != null)
                    currentPropertyDescription = currentClassDescription.getPropertyDescriptionInHierarchy(propertyName);
                if (currentPropertyDescription == null) {
                    visitor.unknownOrAmbigousProperty(currentClassDescription, propertyName);
                    break;
                }
                visitor.property(currentClassDescription, currentPropertyDescription);
                if (currentPropertyDescription instanceof RelationshipPropertyDescription) {
                    currentClassDescription =
                            ((RelationshipPropertyDescription) currentPropertyDescription).getReferencedClassDescription();
                } else {
                    currentClassDescription = null;
                }
            }
        }
    }

}
