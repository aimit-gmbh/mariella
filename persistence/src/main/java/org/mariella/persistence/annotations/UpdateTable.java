package org.mariella.persistence.annotations;

import javax.persistence.UniqueConstraint;

@java.lang.annotation.Target(value = {java.lang.annotation.ElementType.TYPE})
@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface UpdateTable {

    java.lang.String name() default "";

    java.lang.String catalog() default "";

    java.lang.String schema() default "";

    UniqueConstraint[] uniqueConstraints() default {};

}
