package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.database.Converter;
import org.mariella.persistence.schema.ScalarPropertyDescription;


public interface ConverterRegistry {
    Converter<?> getNamedConverter(String converterName);

    Converter<?> getConverterForColumn(
            ScalarPropertyDescription propertyDescription, int sqlType);

    Converter<?> getConverterForColumn(Class<?> propertyType, int sqlType);

    void registerConverterFactory(int sqlType, Class<?> propertyType, ConverterRegistryImpl.ConverterFactory converterFactory);

    void registerBaseTypesOfPropertiesWithConverter(Class<?> propertyBaseType);

    void registerConverter(String converterName, Converter<?> converter);
}
