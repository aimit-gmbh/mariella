package org.mariella.persistence.kotlin.internal

internal class PersistenceXmlGenerator {
    companion object {
        const val NAME: String = "abc"

        private const val TEMPLATE = """<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xmlns="http://java.sun.com/xml/ns/persistence"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd">
    <persistence-unit name="$NAME">
        <properties>
            <property name="org.mariella.persistence.persistenceBuilder"
                      value="{persistenceBuilder}"/>
            <property
                    name="org.mariella.persistence.mapping_builder.DatabaseMetaDataDatabaseInfoProvider.ignoreSchema"
                    value="true"/>
            <property
                    name="org.mariella.persistence.mapping_builder.DatabaseMetaDataDatabaseInfoProvider.ignoreCatalog"
                    value="true"/>
            <property name="org.mariella.persistence.parameter_style" value="{parameterStyle}"/>
            <property name="org.mariella.persistence.db.uppercase" value="false"/>
            <property name="org.mariella.persistence.packages" value="{packages}"/>
        </properties>
    </persistence-unit>
</persistence>
"""
    }

    fun getXml(jdbcUrl: String, packages: List<String>): String {
        val parameterStyle: String
        val packagesString = packages.joinToString(",")
        val persistenceBuilder: String
        if (jdbcUrl.contains("jdbc:h2:")) {
            parameterStyle = "jdbc"
            persistenceBuilder = "org.mariella.persistence.h2.H2PersistenceBuilder"
        } else if (jdbcUrl.contains("jdbc:postgresql:")) {
            parameterStyle = "indexed"
            persistenceBuilder = "org.mariella.persistence.postgres.PostgresPersistenceBuilder"
        } else {
            error("only h2 and postgres is supported")
        }
        return TEMPLATE
            .replace("{packages}", packagesString)
            .replace("{parameterStyle}", parameterStyle)
            .replace("{persistenceBuilder}", persistenceBuilder)
    }

}
