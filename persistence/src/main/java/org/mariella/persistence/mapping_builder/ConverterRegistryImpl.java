package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.database.*;
import org.mariella.persistence.schema.ScalarPropertyDescription;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ConverterRegistryImpl implements ConverterRegistry {
    private final Map<String, Converter<?>> namedConverters = new HashMap<>();
    private final Map<Integer, Map<Class<?>, ConverterFactory>> converterFactories =
            new HashMap<>();
    private final Set<Class<?>> baseTypesOfPropertiesWithConverter = new HashSet<>();

    public ConverterRegistryImpl() {
        super();
        registerConverterFactory(Types.VARCHAR, String.class, new ConverterFactoryImpl(StringConverter.Singleton));
        registerConverterFactory(Types.CHAR, String.class, new ConverterFactoryImpl(StringConverter.Singleton));
        registerConverterFactory(Types.VARBINARY, String.class, new ConverterFactoryImpl(StringConverter.Singleton));
        registerConverterFactory(Types.LONGVARCHAR, String.class, new ConverterFactoryImpl(StringConverter.Singleton));
        registerConverterFactory(Types.OTHER, String.class, new ConverterFactoryImpl(NStringConverter.Singleton));
        registerConverterFactory(Types.BOOLEAN, Boolean.class, new ConverterFactoryImpl(BooleanConverter.Singleton));
        registerConverterFactory(Types.BIT, Boolean.class, new ConverterFactoryImpl(BooleanConverter.Singleton));
        registerConverterFactory(Types.BOOLEAN, boolean.class, new ConverterFactoryImpl(BooleanConverter.Singleton));
        registerConverterFactory(Types.BIT, boolean.class, new ConverterFactoryImpl(BooleanConverter.Singleton));

        registerNumericConverters(Types.INTEGER);
        registerNumericConverters(Types.SMALLINT);
        registerNumericConverters(Types.BIGINT);
        registerNumericConverters(Types.DECIMAL);
        registerNumericConverters(Types.NUMERIC);

        registerConverterFactory(Types.FLOAT, BigDecimal.class, new ConverterFactoryImpl(BigDecimalConverter.Singleton));

        registerConverterFactory(Types.FLOAT, Double.class, new ConverterFactoryImpl(DoubleConverter.Singleton));
        registerConverterFactory(Types.DOUBLE, Double.class, new ConverterFactoryImpl(DoubleConverter.Singleton));
        registerConverterFactory(Types.NUMERIC, Double.class, new ConverterFactoryImpl(DoubleConverter.Singleton));
        registerConverterFactory(Types.DECIMAL, Double.class, new ConverterFactoryImpl(DoubleConverter.Singleton));
        registerConverterFactory(Types.INTEGER, Double.class, new ConverterFactoryImpl(DoubleConverter.Singleton));

        registerConverterFactory(Types.FLOAT, double.class, new ConverterFactoryImpl(DoubleConverter.Singleton));
        registerConverterFactory(Types.DOUBLE, double.class, new ConverterFactoryImpl(DoubleConverter.Singleton));
        registerConverterFactory(Types.NUMERIC, double.class, new ConverterFactoryImpl(DoubleConverter.Singleton));
        registerConverterFactory(Types.DECIMAL, double.class, new ConverterFactoryImpl(DoubleConverter.Singleton));
        registerConverterFactory(Types.INTEGER, double.class, new ConverterFactoryImpl(DoubleConverter.Singleton));

        registerConverterFactory(Types.DATE, Timestamp.class, new ConverterFactoryImpl(TimestampConverter.Singleton));
        registerConverterFactory(Types.TIMESTAMP, Timestamp.class, new ConverterFactoryImpl(TimestampConverter.Singleton));
        registerConverterFactory(Types.DATE, Date.class, new ConverterFactoryImpl(DateConverter.Singleton));
        registerConverterFactory(Types.DATE, java.util.Date.class, new ConverterFactoryImpl(DateConverter.Singleton));
        registerConverterFactory(Types.TIMESTAMP, java.util.Date.class, new ConverterFactoryImpl(DateConverter.Singleton));
        registerConverterFactory(Types.TIMESTAMP, Date.class, new ConverterFactoryImpl(DateConverter.Singleton));

        registerConverterFactory(Types.BLOB, byte[].class, new ConverterFactoryImpl(ByteArrayConverter.Singleton));
        registerConverterFactory(Types.BINARY, byte[].class, new ConverterFactoryImpl(ByteArrayConverter.Singleton));
        registerConverterFactory(Types.VARBINARY, byte[].class, new ConverterFactoryImpl(ByteArrayConverter.Singleton));
        registerConverterFactory(Types.CLOB, char[].class, new ConverterFactoryImpl(CharArrayConverter.Singleton));
        registerConverterFactory(Types.CLOB, String.class, new ConverterFactoryImpl(ClobConverter.Singleton));
    }

    protected void registerNumericConverters(int type) {
        registerConverterFactory(type, Integer.class, new ConverterFactoryImpl(IntegerConverter.Singleton));
        registerConverterFactory(type, int.class, new ConverterFactoryImpl(IntegerConverter.Singleton));
        registerConverterFactory(type, Long.class, new ConverterFactoryImpl(LongConverter.Singleton));
        registerConverterFactory(type, long.class, new ConverterFactoryImpl(LongConverter.Singleton));
        registerConverterFactory(type, BigDecimal.class, new ConverterFactoryImpl(BigDecimalConverter.Singleton));
        registerConverterFactory(type, boolean.class, new ConverterFactoryImpl(BooleanAsNumberConverter.Singleton));
        registerConverterFactory(type, Boolean.class, new ConverterFactoryImpl(BooleanAsNumberConverter.Singleton));
    }

    @Override
    public void registerConverterFactory(int sqlType, Class<?> propertyType, ConverterFactory converterFactory) {
        Map<Class<?>, ConverterFactory> map = converterFactories.computeIfAbsent(sqlType,
                k -> new HashMap<>());
        map.put(propertyType, converterFactory);
    }

    @Override
    public void registerBaseTypesOfPropertiesWithConverter(Class<?> propertyBaseType) {
        baseTypesOfPropertiesWithConverter.add(propertyBaseType);
    }

    @Override
    public void registerConverter(String converterName, Converter<?> converter) {
        namedConverters.put(converterName, converter);
    }

    @Override
    public Converter<?> getNamedConverter(String converterName) {
        Converter<?> converter = namedConverters.get(converterName);
        if (converter == null) {
            throw new IllegalArgumentException("Unknown converter named '" + converterName + "'!");
        } else {
            return converter;
        }
    }

    @Override
    public Converter<?> getConverterForColumn(
            ScalarPropertyDescription propertyDescription, int sqlType) {
        Converter<?> converter = getConverterForColumn(propertyDescription.getPropertyDescriptor().getPropertyType(), sqlType);
        if (converter == null) {
            throw new IllegalArgumentException("Cannot create converter for property " + propertyDescription.getClassDescription()
                    .getClassName() + "." + propertyDescription.getPropertyDescriptor().getName());
        }
        return converter;
    }

    @Override
    public Converter<?> getConverterForColumn(Class<?> propertyType, int sqlType) {
        Map<Class<?>, ConverterFactory> map = converterFactories.get(sqlType);
        if (map != null) {

            Converter<?> converter = getConverter(map, propertyType, sqlType, propertyType);

            /* no converter found for property type -> search for converter of basetype of property */
            if (converter == null) {
                Class<?> baseTypeOfProperty = getBaseTypeOfPropertyWithConverter(propertyType);
                if (baseTypeOfProperty != null) {
                    converter = getConverter(map, propertyType, sqlType, baseTypeOfProperty);
                }
            }
            if (converter != null) {
                return converter;
            }
        }

        /* standard - enum */
        if (propertyType.isEnum()) {
            return getEnumConverter(sqlType, propertyType);
        }

        return null;
    }

    protected Converter<?> getConverter(Map<Class<?>, ConverterFactory> map, Class<?> propertyType,
                                        int sqlType, Class<?> propertyBaseType) {
        ConverterFactory factory = map.get(propertyBaseType);
        if (factory != null) {
            return factory.createConverter(propertyType, sqlType);
        }
        return null;
    }

    protected Class<?> getBaseTypeOfPropertyWithConverter(Class<?> propertyType) {
        Class<?> superClass = propertyType.getSuperclass();
        if (superClass != null) {
            if (baseTypesOfPropertiesWithConverter.contains(superClass)) {
                return superClass;
            } else {
                Class<?> foundClass = getBaseTypeOfPropertyWithConverter(superClass);
                if (foundClass != null) {
                    return foundClass;
                }
            }
        }
        Class<?>[] interfaces = propertyType.getInterfaces();
        for (Class<?> interf : interfaces) {
            if (baseTypesOfPropertiesWithConverter.contains(interf)) {
                return interf;
            } else {
                Class<?> foundClass = getBaseTypeOfPropertyWithConverter(interf);
                if (foundClass != null) {
                    return foundClass;
                }
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Converter<?> getEnumConverter(int columnType, Class<?> propertyType) {
        if ((columnType == Types.VARCHAR || columnType == Types.CHAR || columnType == Types.LONGVARCHAR)) {
            return new EnumConverter(propertyType);
        } else {
            return null;
        }
    }

    public interface ConverterFactory {
        Converter<?> createConverter(Class<?> propertyDescription,
                                     int sqlType);
    }

    public static class ConverterFactoryImpl implements ConverterFactory {
        private final Converter<?> converter;

        public ConverterFactoryImpl(Converter<?> converter) {
            super();
            this.converter = converter;
        }

        @Override
        public Converter<?> createConverter(Class<?> propertyDescription,
                                            int sqlType) {
            return converter;
        }
    }

}
