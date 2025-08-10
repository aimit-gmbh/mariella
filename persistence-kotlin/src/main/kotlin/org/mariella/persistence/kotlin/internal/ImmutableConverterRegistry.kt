package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.database.Converter
import org.mariella.persistence.mapping_builder.ConverterRegistry
import org.mariella.persistence.mapping_builder.ConverterRegistryImpl
import org.mariella.persistence.schema.ScalarPropertyDescription
import java.sql.JDBCType

class ImmutableConverterRegistry internal constructor(private val converterRegistry: ConverterRegistry) : ConverterRegistry {
    override fun getNamedConverter(converterName: String): Converter<*> {
        return converterRegistry.getNamedConverter(converterName) ?: error("converter $converterName not registered")
    }

    override fun getConverterForColumn(propertyDescription: ScalarPropertyDescription, sqlType: Int): Converter<*> {
        return converterRegistry.getConverterForColumn(propertyDescription, sqlType)
            ?: error("converter for scalar property ${propertyDescription.propertyDescriptor.name} and sql type $sqlType not registered")
    }

    override fun getConverterForColumn(propertyType: Class<*>, sqlType: Int): Converter<*> {
        return converterRegistry.getConverterForColumn(propertyType, sqlType)
            ?: error("converter for class ${propertyType.name} and sql type $sqlType not registered")
    }

    override fun registerConverterFactory(
        sqlType: Int,
        propertyType: Class<*>?,
        converterFactory: ConverterRegistryImpl.ConverterFactory?
    ) {
        throw UnsupportedOperationException()
    }

    override fun registerBaseTypesOfPropertiesWithConverter(propertyBaseType: Class<*>?) {
        throw UnsupportedOperationException()
    }

    override fun registerConverter(converterName: String?, converter: Converter<*>?) {
        throw UnsupportedOperationException()
    }

    fun getConverterForColumn(clazz: Class<*>, type: JDBCType): Converter<*> {
        return getConverterForColumn(
            clazz,
            type.vendorTypeNumber
        )
    }

}