package org.mariella.persistence.database;

import org.mariella.persistence.query.Literal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public class DateToStringConverter implements Converter<Timestamp> {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateToStringConverter INSTANCE = new DateToStringConverter();
    private static final Logger LOGGER = LoggerFactory.getLogger(DateToStringConverter.class);


    private DateToStringConverter() {

    }

    public static DateToStringConverter getInstance() {
        return INSTANCE;
    }

    @Override
    public void setObject(ParameterValues pv, int index, Timestamp value) {
        pv.setString(index, value != null ? FORMAT.format(value) : null);
    }

    @Override
    public Timestamp getObject(ResultRow row, int index) {
        String s = row.getString(index);
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return new Timestamp(FORMAT.parse(s).getTime());
        } catch (ParseException e) {
            LOGGER.warn("Invalid date string: {}", s);
            return null;
        }
    }

    @Override
    public Literal<Timestamp> createLiteral(Object value) {
        return new TimestampLiteral((Timestamp) value);
    }

    @Override
    public Literal<Timestamp> createDummy() {
        return createLiteral(new Timestamp(0));
    }

    @Override
    public String toString(Timestamp value) {
        if (value == null) {
            return "NULL";
        } else {
            return String.format("DATE '%s'", FORMAT.format(value));
        }
    }

    private static class TimestampLiteral extends Literal<Timestamp> {
        public TimestampLiteral(Timestamp value) {
            super(DateToStringConverter.getInstance(), value);
        }
    }
}
