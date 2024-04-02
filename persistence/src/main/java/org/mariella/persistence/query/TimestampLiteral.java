package org.mariella.persistence.query;

import org.mariella.persistence.database.Converter;
import org.mariella.persistence.database.TimestampConverter;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class TimestampLiteral extends Literal<Timestamp> {

    public TimestampLiteral(Timestamp value) {
        super(TimestampConverter.Singleton, value);
    }

    public TimestampLiteral(Converter<Timestamp> converter, Timestamp value) {
        super(converter, value);
    }

    public void printSql(StringBuilder b) {
        if (value == null) {
            b.append("NULL");
        } else {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:");
            String string = format.format(value);
            DecimalFormat df = new DecimalFormat("000000000");
            b.append("TO_TIMESTAMP('");
            b.append(string);
            b.append(df.format(value.getNanos()));
            b.append("', ");
            b.append("'dd.mm.yyyy HH24:MI:SS:FF9')");
        }
    }

}
